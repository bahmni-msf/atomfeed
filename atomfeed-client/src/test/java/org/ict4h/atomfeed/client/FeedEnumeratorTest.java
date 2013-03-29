package org.ict4h.atomfeed.client;

import com.sun.syndication.feed.atom.Entry;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.datasource.WebClientStub;
import org.ict4h.atomfeed.server.domain.chunking.number.NumberChunkingHistory;
import org.ict4h.atomfeed.server.repository.AllEventRecords;
import org.ict4h.atomfeed.server.repository.AllEventRecordsStub;
import org.ict4h.atomfeed.server.repository.InMemoryEventRecordCreator;
import org.ict4h.atomfeed.server.service.EventFeedService;
import org.ict4h.atomfeed.server.service.EventFeedServiceImpl;
import org.ict4h.atomfeed.server.service.feedgenerator.FeedGenerator;
import org.ict4h.atomfeed.server.service.feedgenerator.NumberFeedGenerator;
import org.ict4h.atomfeed.spring.resource.EventResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class FeedEnumeratorTest {
	
	private InMemoryEventRecordCreator feedRecordCreator;
	private WebClientStub webClientStub;
	private AllFeeds allFeeds;

	@Before
	public void setUp() {
		AllEventRecordsStub allEventRecords = new AllEventRecordsStub();
        FeedGenerator generator = getFeedGenerator(allEventRecords);
        EventFeedService eventFeedService = new EventFeedServiceImpl(generator);
        webClientStub = new WebClientStub(new EventResource(eventFeedService));
        feedRecordCreator = new InMemoryEventRecordCreator(allEventRecords);
        allFeeds = new AllFeeds(webClientStub);
	}
	
	private FeedGenerator getFeedGenerator(AllEventRecords eventRecords) {
		NumberChunkingHistory config = new NumberChunkingHistory();
        config.add(1, 5, 1);
        return new NumberFeedGenerator(eventRecords, config);
	}
	
    @Test
    public void shouldGetAllEntries() throws URISyntaxException {
        feedRecordCreator.create(7);
        FeedEnumerator feedEnumerator = new FeedEnumerator(allFeeds, new URI("http://foo.bar/2"));
        List<Entry> entries = feedEnumerator.getAllEntries();
        Assert.assertEquals(7, entries.size());
        Assert.assertEquals("tag:atomfeed.ict4h.org:uuid1", entries.get(0).getId());
        Assert.assertEquals("tag:atomfeed.ict4h.org:uuid7", entries.get(6).getId());
    }
    
    @Test
    public void shouldGetLastTwoEntries() throws URISyntaxException {
    	feedRecordCreator.create(7);
        FeedEnumerator feedEnumerator = new FeedEnumerator(allFeeds, new URI("http://foo.bar/2"));
        //the IDs are created by feedRecordCreator as uuid1, uuid2 etc 
        List<Entry> entries = feedEnumerator.newerEntries("tag:atomfeed.ict4h.org:uuid5");
        Assert.assertEquals(2, entries.size());
    }
    
    @Test
    public void shouldGetNotFindAnyNewEntry() throws URISyntaxException {
    	feedRecordCreator.create(7);
        FeedEnumerator feedEnumerator = new FeedEnumerator(allFeeds, new URI("http://foo.bar/2"));
        //the IDs are created by feedRecordCreator as uuid1, uuid2 etc 
        List<Entry> entries = feedEnumerator.newerEntries("tag:atomfeed.ict4h.org:uuid7");
        Assert.assertEquals(0, entries.size());
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldErrorOutOnInvalidStartingURL() throws URISyntaxException {
    	feedRecordCreator.create(7);
        FeedEnumerator feedEnumerator = new FeedEnumerator(allFeeds, new URI("http://foo.bar/4"));
        feedEnumerator.newerEntries("tag:atomfeed.ict4h.org:uuid7");
    }
    
    @Test(expected=RuntimeException.class)
    public void shouldErrorOutForInvalidEntry() throws URISyntaxException {
    	feedRecordCreator.create(7);
        FeedEnumerator feedEnumerator = new FeedEnumerator(allFeeds, new URI("http://foo.bar/2"));
        feedEnumerator.newerEntries("invalidentryid");
    }
}