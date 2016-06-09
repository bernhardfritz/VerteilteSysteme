import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DistGrep {

	public static String regex;
	
public static class RegexFilter extends Configured implements PathFilter {
 
    Pattern pattern;
    Configuration conf;
    FileSystem fs;
 
    @Override
    public boolean accept(Path path) {
 
        try {
            if (fs.isDirectory(path)) {
                return true;
            } else {
                Matcher m = pattern.matcher(path.toString());
                System.out.println("Is path : " + path.toString() + " matches "
                        + conf.get("file.pattern") + " ? , " + m.matches());
                return m.matches();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
 
    }
 
    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        if (conf != null) {
            try {
                fs = FileSystem.get(conf);
                pattern = Pattern.compile(conf.get("file.pattern"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
 
}


  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
     // StringTokenizer itr = new StringTokenizer(value.toString());
      //while (itr.hasMoreTokens()) {
        //word.set(itr.nextToken());
    	word.set(value.toString());
        context.write(word, one);
      //}
    }
  }

  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
	  
    DistGrep.regex = args[2];

    Configuration conf = new Configuration();
    conf.set("file.pattern", args[2]);

    Job job = Job.getInstance(conf, "DistGrep");
    job.setJarByClass(DistGrep.class);
    
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    
    
    job.setOutputKeyClass(Text.class);
    
    
    job.setOutputValueClass(IntWritable.class);
    
    //input path


    
    FileInputFormat.setInputPathFilter(job, RegexFilter.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));

    //output path
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
   
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
