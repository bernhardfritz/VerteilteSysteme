import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class DistGrep {

	public static class RegexFilter extends Configured implements PathFilter {
		private Pattern pattern;
		private Configuration conf;
		private FileSystem fs;
	 
	    @Override
	    public boolean accept(Path path) {
	 
	        try {
	            if (fs.isDirectory(path)) {
	                return true;
	            } else {
	                Matcher m = pattern.matcher(path.toString());
	                System.out.println("Is path : " + path.toString() + " matches " + conf.get("file.pattern") + " ? , " + m.matches());
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
	
	public static class ReverseSortComparator extends WritableComparator {
		
		public ReverseSortComparator() {
			super(IntWritable.class, true);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public int compare(WritableComparable a, WritableComparable b) {
			return a.compareTo(b) * (-1);
		}
	}

	public static class TextMapper extends Mapper<Object, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text line = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			line.set(value.toString());
			context.write(line, one);
		}
	}

	public static class TextReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}
	
	public static class CountMapper extends Mapper<Object, Text, IntWritable, Text> {
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			int index = value.toString().lastIndexOf("\t") + 1;
			IntWritable count = new IntWritable(Integer.parseInt(value.toString().substring(index)));
			word.set(value.toString().substring(0, index));
			context.write(count, word);
		}
	}
	
	public static class CountReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			context.write(result, key);
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			System.err.println("Usage: DistGrep <input_path> <temp_path> <output_path> <regex>");
			System.exit(1);
		}
		
		Configuration conf = new Configuration();
		conf.set("file.pattern", args[3]);
		
		/*	Job 1	*/

		Job job1 = Job.getInstance(conf, "DistGrep");
		job1.setJarByClass(DistGrep.class);
    
		job1.setMapperClass(TextMapper.class);
		job1.setReducerClass(TextReducer.class);
    
		job1.setOutputKeyClass(Text.class);
		job1.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.setInputPathFilter(job1, RegexFilter.class);
		FileInputFormat.addInputPath(job1, new Path(args[0]));
		FileOutputFormat.setOutputPath(job1, new Path(args[1]));
   
		job1.waitForCompletion(true);
		
		
		/*	Job 2	*/
		
		Job job2 = Job.getInstance(conf, "DistGrep");
		job2.setJarByClass(DistGrep.class);
    
		job2.setMapperClass(CountMapper.class);
		job2.setReducerClass(CountReducer.class);
		job2.setSortComparatorClass(ReverseSortComparator.class);
    
		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));
		
    	System.exit(job2.waitForCompletion(true) ? 0 : 1);
	}
}