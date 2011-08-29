/**
 * Copyright 2009 Takahiko Ito
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
 * SelectClustersMapper.
 */
public class SelectClustersMapper extends
        Mapper<LongWritable, Text, SeedClusterId, RelatedUsersWritable> {


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
     * @param value containing id and the features
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

            StringTokenizer image_id_tokenizer = tokenize(value, " %");

            String image_features = image_id_tokenizer.nextToken();
            String image_id = image_id_tokenizer.nextToken();

            StringTokenizer features_tokenizer = tokenize(image_features, " ()[]:,");
            int number_of_features = features_tokenizer.countTokens() / 2;
            double square_sum = 0;
            int[] feature_id = new int[number_of_features];
            double[] feature_value = new double[number_of_features];

            int count = 0;
            while (features_tokenizer.hasMoreTokens()) {
                feature_id[count] = Integer.parseInt(features_tokenizer.nextToken());
                feature_value[count] = Double.parseDouble(features_tokenizer.nextToken());
                count++;
            }

            StringBuffer strbuf = new StringBuffer();

            for (int i = 0; i < feature_id.length; i++) {
                if (feature_value[i] > 0) {
                    strbuf.append(feature_id[i]);
                }
                if (i > 0)
                    strbuf.append(" ");
            }

            Long id = Long.parseLong(image_id); // example id
            Set<Long> featureSet 
                = this.extractFeatures(strbuf.toString());
            
            for (int i=0; i<seedsAry.length; i++) {
                LongWritable clusterId 
                    = this.function.returnClusterId(featureSet, 
                        seedsAry[i]);
                context.write(new SeedClusterId(
                        seedsAry[i], clusterId.get()), 
                        new RelatedUsersWritable(id)); 
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("PARSING ERROR in line: " + inputStr);
            e.printStackTrace();
        }
    }
    
    /**
     * Extract features from feature string.
     * 
     * @param featureStr string containing feature information
     * @return map containing feature and the value
     */
    private Set<Long> extractFeatures(
            final String featureStr) {
        Set<Long> rtSet = new HashSet<Long>();
        String[] featureArray = featureStr.split(" ");
        for (int i=0; i<featureArray.length; i++) {
            rtSet.add(Long.parseLong(featureArray[i]));
        }
        return rtSet;
    }
    
    /**
     * setup.
     * @param context context
     */
    public final void setup(final Context context) {
        Configuration jc = context.getConfiguration();

        /* create a object implements IHashFunction */
        String functionClassName 
            = LikelikeConstants.DEFAULT_HASH_FUNCTION;
        if (context == null || jc == null) {
            /* added default configuration for testing */
            jc = new Configuration();
        }
        try {
            functionClassName = jc.get(
                    LikelikeConstants.HASH_FUNCTION,
                    LikelikeConstants.DEFAULT_HASH_FUNCTION);
            Class<? extends IHashFunction> functionClass 
                = Class.forName(
                    functionClassName).asSubclass(
                            IHashFunction.class);
            Constructor<? extends IHashFunction> constructor 
                = functionClass
                    .getConstructor(Configuration.class);
            function = constructor.newInstance(jc);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException(nsme);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        } catch (InstantiationException ie) {
            throw new RuntimeException(ie);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        } catch (InvocationTargetException ite) {
            throw new RuntimeException(ite.getCause());
        }
        
        /* extract set of hash seeds */
        String seedsStr = jc.get(MINWISE_HASH_SEEDS, 
                DEFAULT_MINWISE_HASH_SEEDS);
        String[] seedsStrAry = seedsStr.split(":");
        this.seedsAry = new long[seedsStrAry.length];
        for (int i =0; i< seedsStrAry.length; i++) {
            this.seedsAry[i] =Long.parseLong(seedsStrAry[i]);
        }
    }
        
    /** Hash function object. */
    private IHashFunction function;
    
    /** Set of hash seeds. */
    private long[] seedsAry; 

    /** Symbol: hash seed. */
    public static final String MINWISE_HASH_SEEDS
        = "likelike.minwise.hash.seedS";
    
    /** Default: hash seed. */
    public static final String DEFAULT_MINWISE_HASH_SEEDS    
        = "1";        
    
}
