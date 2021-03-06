package org.sagebionetworks.bridge.workers.dynamodb.streams;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.model.Record;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.dynamodbv2.streamsadapter.model.RecordAdapter;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;

public class StreamRecordProcessorTest {

    private StreamRecordProcessor processor;

    @Before
    public void before() {
        processor = new StreamRecordProcessor(2) {
            @Override
            Logger log() {
                return LoggerFactory.getLogger(getClass());
            }
            @Override
            void onInsert(Record streamRecord) {
                assertEquals("INSERT", streamRecord.getEventName());
            }
            @Override
            void onModify(Record streamRecord) {
                assertEquals("MODIFY", streamRecord.getEventName());
            }
            @Override
            void onRemove(Record streamRecord) {
                assertEquals("REMOVE", streamRecord.getEventName());
            }
        };
    }

    @Test
    public void test() throws Exception {
        final List<com.amazonaws.services.kinesis.model.Record> records = new ArrayList<>();
        records.add(mockRecordAdapter("0", "INSERT"));
        records.add(mockRecordAdapter("1", "MODIFY"));
        records.add(mockRecordAdapter("2", "REMOVE"));
        IRecordProcessorCheckpointer checkpointer = mock(IRecordProcessorCheckpointer.class);
        processor.processRecords(records, checkpointer);
        verify(checkpointer, times(1)).checkpoint();
    }

    private RecordAdapter mockRecordAdapter(final String eventId, final String eventName) {
        Record record = mock(Record.class);
        when(record.getEventID()).thenReturn(eventId);
        when(record.getEventName()).thenReturn(eventName);
        StreamRecord streamRecord = mock(StreamRecord.class);
        when(streamRecord.getSequenceNumber()).thenReturn(eventId);
        when(record.getDynamodb()).thenReturn(streamRecord);
        RecordAdapter recordAdapter = mock(RecordAdapter.class);
        when(recordAdapter.getInternalObject()).thenReturn(record);
        return recordAdapter;
    }
}
