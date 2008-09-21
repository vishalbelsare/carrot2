package org.carrot2.source.boss;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.carrot2.core.*;
import org.carrot2.core.attribute.Init;
import org.carrot2.core.attribute.Processing;
import org.carrot2.source.MultipageSearchEngine;
import org.carrot2.source.SearchEngineResponse;
import org.carrot2.util.ExecutorServiceUtils;
import org.carrot2.util.attribute.*;
import org.carrot2.util.attribute.constraint.ImplementingClasses;

/**
 * A {@link DocumentSource} fetching {@link Document}s (search results) from <a
 * href="http://developer.yahoo.com/search/boss/">Yahoo BOSS</a>.
 */
@Bindable(prefix = "BossDocumentSource")
public final class BossDocumentSource extends MultipageSearchEngine
{
    /** Logger for this class. */
    final static Logger logger = Logger.getLogger(BossDocumentSource.class);

    /**
     * Maximum concurrent threads from all instances of this component.
     */
    private static final int MAX_CONCURRENT_THREADS = 5;

    /**
     * Static executor for running search threads.
     */
    private final static ExecutorService executor = ExecutorServiceUtils
        .createExecutorService(MAX_CONCURRENT_THREADS, BossDocumentSource.class);

    /**
     * Determines whether to keep the original query word highlights. Yahoo by default
     * highlights query words in search results using the &lt;b&gt; HTML tag. Set this
     * attribute to <code>true</code> to keep these highlights.
     * 
     * @group Postprocessing
     * @level Advanced
     * @label Keep highlights
     */
    @Input
    @Processing
    @Attribute
    public boolean keepHighlights = false;

    /**
     * The specific search service to be used by this document source. Use this attribute
     * to choose which BOSS's service to query, e.g. Web, News or Image search.
     * 
     * @label Boss Search Service
     * @level Advanced
     * @group Service
     */
    @Init
    @Input
    @Attribute
    @ImplementingClasses(classes =
    {
        BossWebSearchService.class, BossNewsSearchService.class,
        BossImageSearchService.class
    })
    public BossSearchService service = new BossWebSearchService();

    /**
     * Run a single query.
     */
    @Override
    public void process() throws ProcessingException
    {
        super.process(service.metadata, executor);
    }

    /**
     * Create a single page fetcher for the search range.
     */
    @Override
    protected final Callable<SearchEngineResponse> createFetcher(final SearchRange bucket)
    {
        return new SearchEngineResponseCallable()
        {
            public SearchEngineResponse search() throws Exception
            {
                return service.query(query, bucket.start, bucket.results);
            }
        };
    }

    @Override
    protected void afterFetch(SearchEngineResponse response)
    {
        clean(response, keepHighlights, Document.TITLE, Document.SUMMARY);
    }
}
