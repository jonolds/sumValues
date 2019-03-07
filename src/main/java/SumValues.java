import java.io.IOException;
import java.util.stream.StreamSupport;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class SumValues {
	static final String project = "cc5";
	static String delim = "\t";
	
	
	static Text keyText = new Text("Sum of values");
	static String valSumOutput = "";
	
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {
		
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {			
			
			if(project.equals("cc5")) {
				for(String s : value.toString().split(delim)[1].split(";"))
					if(s.length() > 0)
						context.write(new Text("Total word count: "), new IntWritable(Integer.parseInt(s.substring(6, s.length()))));
			}
			else {
				String[] tabSplit = value.toString().split(delim);
				if(tabSplit.length == 2)
					context.write(keyText, new IntWritable(Integer.parseInt(tabSplit[1])));
			}
		}
	}
	
	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();
		
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			int sum = StreamSupport.stream(values.spliterator(), false).mapToInt(x->x.get()).sum();
			result.set(sum);
			context.write(key, result);
			valSumOutput = keyText.toString() + "\t" + sum;
		}
	}
	
	public static void main(String[] args) throws Exception {
		Job job = initializeJob(args);
		job.setJarByClass(SumValues.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path("C://cloud//" + project + "//output"));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setNumReduceTasks(1);
		System.out.println(job.waitForCompletion(true) ? "Job SUCCESS" : "Job FAILED");
		System.out.println(valSumOutput);
		System.exit(0);
	}
	
	public static Job initializeJob(String[] args) throws IOException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: wordcount <in> <out>");
			System.exit(2);
		}
		new Path(args[1]).getFileSystem(conf).delete(new Path(otherArgs[1]), true);
		return Job.getInstance(conf, "SumValues");
	}
}