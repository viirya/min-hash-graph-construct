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
import java.io.*;
import java.util.*;
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
 * ReassignClusterIDsReducer. 
 */
public class ReassignClusterIDsReducer extends Reducer<Text, Text, Text, Text> {

    public static StringTokenizer tokenize(String line, String pattern) {
        StringTokenizer tokenizer = new StringTokenizer(line, pattern);
        return tokenizer;
    }

    public static StringTokenizer tokenize(Text value, String pattern) {
        String line = value.toString();
        StringTokenizer tokenizer = new StringTokenizer(line, pattern);
        return tokenizer;
    }

    
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

        String reassigned_cluster_id = null;
        String images = null;
       
        for (Text value: values) {

            StringTokenizer cluster_id_tokenizer = tokenize(value, "\t");

            String cluster_id = cluster_id_tokenizer.nextToken();
            String images_or_count = cluster_id_tokenizer.nextToken();

            StringTokenizer images_tokenizer = tokenize(images_or_count, ":");

            if (images_tokenizer.countTokens() == 2) {
                // count

                images_tokenizer.nextToken();
                reassigned_cluster_id = images_tokenizer.nextToken();

            } else if (images_tokenizer.countTokens() == 1) {
                // images

                images = images_or_count;

            }

            if (reassigned_cluster_id != null && images != null)
                context.write(new Text(reassigned_cluster_id), new Text(images));

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

