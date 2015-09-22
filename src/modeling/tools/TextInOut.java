package modeling.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TextInOut {
	public static ArrayList<String> readFile(String filename)
			throws IOException {
		ArrayList<String> res = new ArrayList<>();
		BufferedReader input = new BufferedReader(new FileReader(filename));

		String line = null;
		while ((line = input.readLine()) != null) {
			res.add(line);
		}

		input.close();
		return res;
	}

	public static void writeFile(ArrayList<String> data, String filename)
			throws IOException {
		writeFile(data, filename, false);
	}
	
	public static void writeFile(String data, String filename) throws IOException
	{
		ArrayList<String> list = new ArrayList<String>();
		list.add(data);
		writeFile(list, filename, false);
	}

	public static void writeFile(ArrayList<String> data, String filename,
			boolean append) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(filename,
				append));

		for (String line : data) {
			output.write(line);
			output.write("\r\n");
		}

		output.close();
	}

}
