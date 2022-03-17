//UNIQUE LISTENERS
package mapreduce;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Music_website 
{
 private enum COUNTERS 
        {
  INVALID_RECORD_COUNTS
 }

 private static class MusicMapper extends Mapper<Object, Text, IntWritable, IntWritable> {
  IntWritable user_id = new IntWritable();
  IntWritable track_id = new IntWritable();
  @Override
  public void map(Object key, Text value,
    Mapper<Object, Text, IntWritable, IntWritable>.Context context) throws IOException, InterruptedException {

   String line = value.toString();
   String tokens[] = line.split("[|]");

   user_id.set(Integer.parseInt(tokens[Constants.USER_ID]));
   track_id.set(Integer.parseInt(tokens[Constants.TRACK_ID]));

   if (tokens.length == 5) {
    context.write(track_id, user_id);
   } else {
    context.getCounter(COUNTERS.INVALID_RECORD_COUNTS).increment(1L);
   }
  }
 }

 private static class MusicReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
  @Override
  public void reduce(
    IntWritable trackid,
    Iterable<IntWritable> userids,
    Reducer<IntWritable, IntWritable, IntWritable, IntWritable>.Context context) throws IOException, InterruptedException {

   Set<Integer> useridset = new HashSet<>();
   for (IntWritable userID : userids) {
    useridset.add(userID.get());
   }
   context.write(trackid, new IntWritable(useridset.size()));
  }
 }

 public static void main(String[] args) throws IOException,ClassNotFoundException, InterruptedException {
  Configuration conf = new Configuration();
  Job job = Job.getInstance(conf, "Music_website");

  job.setJarByClass(Music_website.class);
  job.setMapperClass(MusicMapper.class);
  job.setReducerClass(MusicReducer.class);

  job.setOutputKeyClass(IntWritable.class);
  job.setOutputValueClass(IntWritable.class);

  FileInputFormat.addInputPath(job, new Path(args[0]));
  FileOutputFormat.setOutputPath(job, new Path(args[1]));

  System.exit(job.waitForCompletion(true) ? 0 : 1);
 }
}
