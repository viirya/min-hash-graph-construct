
# Using MinHash to fast construct image graph

This repos contains the codes modified from [LikeLike project](http://code.google.com/p/likelike/). In order to construct precise image graph, pairwise similarity computation is needed for each pair of images. This process is slow when dealing with large-scale image collection, even implemented on Hadoop platform. A faster approach to construct image graph is to leverage hashing techniques such LSH (locality sensitive hashing). LikeLike project implemented MinHash, one of LSH algorithm, on Hadoop platform for item recommendation or nearest neighbor extraction. This repos takes the usage of nearest neighbor computation to find roughly those NN images for each image. A rough image graph can be constructed in this way, although the image will be too sparse. 


