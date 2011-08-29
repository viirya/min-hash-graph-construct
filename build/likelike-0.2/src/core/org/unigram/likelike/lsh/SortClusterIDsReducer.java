/**
 * Copyright 2010 Liang-Chi Hsieh
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *        
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.unigram.likelike.lsh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.common.RelatedUsersWritable;
import org.unigram.likelike.common.SeedClusterId;

/**
 * SortClusterIDsReducer. 
 */
public class SortClusterIDsReducer extends Reducer<Text, Text, Text, Text> {
    
    /**
     * Reduce.
     *
     * @param key dummy
     * @param values cluster ids
     * @param context -
     * @throws IOException -
     * @throws InterruptedException -
     */
    @Override
    public void reduce(final Text key,
            final Iterable<Text> values,
            final Context context)
            throws IOException, InterruptedException {
       
        int count = 0;
        for (Text cluster: values) {
            context.write(cluster, new Text("count:" + Integer.toString(count++)));
        }
    }
   
    /**
     * setup.
     * @param context -
     */
    @Override
    public final void setup(final Context context) {

        Configuration jc = null;

        if (context != null) {
            jc = context.getConfiguration();
        }

        if (jc == null) {
            jc = new Configuration();
        }
    }
   
}

