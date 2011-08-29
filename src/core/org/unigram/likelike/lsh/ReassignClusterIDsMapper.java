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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.unigram.likelike.common.LikelikeConstants;
import org.unigram.likelike.common.RelatedUsersWritable;
import org.unigram.likelike.common.SeedClusterId;
import org.unigram.likelike.lsh.function.IHashFunction;

/**
 * ReassignClusterIDsMapper.
 */
public class ReassignClusterIDsMapper extends
        Mapper<LongWritable, Text, Text, Text> {


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
     * map.
     * @param key dummy
     * @param value containing (cluster id and the image ids) and (cluster id and cluster count)
     * @param context context 
     * @exception IOException -
     * @exception InterruptedException -
     */
    @Override
    public final void map(final LongWritable key,
            final Text value, final Context context)
            throws IOException, InterruptedException {
        String inputStr = value.toString();

        try {

            StringTokenizer cluster_id_tokenizer = tokenize(value, "\t");

            String cluster_id = cluster_id_tokenizer.nextToken();
            String images_or_count = cluster_id_tokenizer.nextToken();

            context.write(new Text(cluster_id), new Text(images_or_count));

        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("PARSING ERROR in line: " + inputStr);
            e.printStackTrace();
        }
    }
    
    /**
     * setup.
     * @param context context
     */
    public final void setup(final Context context) {
        Configuration jc = context.getConfiguration();

    }
        
}
