package org.carrot2.output.metrics;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.carrot2.core.Document;

import com.google.common.collect.Sets;

/**
 * Some methods useful when calculating cluster quality metrics.
 */
class ClusteringMetricUtils
{
    /**
     * Returns the number of distinct {@link Document#TOPIC}s in a collection of
     * documents. Note if that at least one of the document has a <code>null</code> topic,
     * 0 will be returned.
     */
    static int countTopics(List<Document> documents)
    {
        final HashSet<String> topics = Sets.newHashSet();
        for (Document document : documents)
        {
            final String topic = document.getField(Document.TOPIC);
            if (StringUtils.isBlank(topic))
            {
                return 0;
            }
            topics.add(topic);
        }
        return topics.size();
    }
}
