// TRACK SHARED
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

public class Share_count {
 private enum COUNTERS
       {
  INVALID_RECORD_COUNTS
 }

 private static class MusicMapper_share extends
   Mapper<Object, Text, IntWritable, IntWritable> {
  // Output object variables
  IntWritable track_id = new IntWritable();
  IntWritable share_id = new IntWritable();

  @Override
  public void map(Object key, Text value,
    Mapper<Object, Text, IntWritable, IntWritable>.Context context)
    throws IOException, InterruptedException {

   String line = value.toString();


   String tokens[] = line.split("[|]");

   track_id.set(Integer.parseInt(tokens[Music_website_constants.TRACK_ID]));
   share_id.set(Integer.parseInt(tokens[Music_website_constants.IS_SHARED]));
   if (share_id.get() == 1) {
    if (tokens.length == 5) {
     context.write(track_id, share_id);
    } else {
     context.getCounter(COUNTERS.INVALID_RECORD_COUNTS).increment(1L);
    }
   }

  }
 }

 private static class MusicReducer_share extends Reducer<IntWritable, IntWritable, IntWritable,IntWritable> {

  @Override
  public void reduce(
    IntWritable trackid,
    Iterable<IntWritable> share_ids,
    Reducer<IntWritable, IntWritable, IntWritable, IntWritable>.Context context)
    throws IOException, InterruptedException {
   Set<Integer> share_idset = new HashSet<>();
   for (IntWritable shareID : share_ids) {
    share_idset.add(shareID.get());
   }
   context.write(trackid, new IntWritable(share_idset.size()));
  }
 }

 public static void main(String[] args) throws IOException,ClassNotFoundException,InterruptedException {
  Configuration conf = new Configuration();
  Job job = Job.getInstance(conf, "Share_count");

  job.setJarByClass(Share_count.class);
  job.setMapperClass(MusicMapper_share.class);
  job.setReducerClass(MusicReducer_share.class);

  job.setOutputKeyClass(IntWritable.class);
  job.setOutputValueClass(IntWritable.class);

  FileInputFormat.addInputPath(job, new Path(args[0]));
  FileOutputFormat.setOutputPath(job, new Path(args[1]));

  System.exit(job.waitForCompletion(true) ? 0 : 1);
 }

}