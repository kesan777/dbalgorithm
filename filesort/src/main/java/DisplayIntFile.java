import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

public class DisplayIntFile {

	public static void main(String[] args) throws IOException {

		// 根据文件名读取数据,注意根据数据量调整缓冲区大小以及
		String fileName = "merge.30";
		ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024 * 4  * 4);
		FileChannel fileChannel = FileChannel.open(Paths.get(fileName));
		fileChannel.read(buffer);
		buffer.flip();

		int[] arr = new int[1024 * 1024 * 4];

		int filled = FileSortMain.fillArray(arr, 0 , buffer);

		System.out.println(filled);

		int pre = arr[0];

		for(int i = 0; i < filled ; i++){
			//if(pre > arr[i]){
			//	System.out.println(i+"=" + arr[i]);
			//	throw new RuntimeException("检测到错误的数据");
			//}
			System.out.println(arr[i]);
			pre = arr[i];
		}

	}
}
