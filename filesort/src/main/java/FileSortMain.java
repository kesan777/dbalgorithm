import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

public class FileSortMain {


	private static Integer bufferSize = 0;

	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("参数不完整,完整的参数如下所示");
			System.out.println("java -jar xx.jar [缓冲区大小] [文件路径]");
			return;
		}

		try {
			bufferSize = Integer.parseInt(args[0]);
		}catch (Exception ex){
			System.out.println("请输入正确的缓存区参数!");
		}

		if(bufferSize % 4 != 0){
			System.out.println("缓冲区的大小必须为4的倍数!");
			return;
		}

		long startUp = System.currentTimeMillis();

		String filePath = args[1];

		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

		System.out.println("任务简报");

		System.out.println("缓冲区大小 -> " + bufferSize+"b");

		System.out.println("目标文件 -> " + filePath);
		FileChannel fileChannel = null;

		try {
			 fileChannel = FileChannel.open(Paths.get(filePath));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			fileChannel.read(byteBuffer);
		 	merge( sortAndSplitFile(byteBuffer, fileChannel) );

		 	System.out.println("排序完成,总计耗时:" + (System.currentTimeMillis() - startUp) + "ms");

		} catch (IOException e) {
			System.out.println("读取数据时发生错误!");
			return;
		}
	}

	/**
	 * description 分片，对原始文件中的数据执行分片操作
	 * @param buffer
	 * @param fileChannel
	 */
	private static LinkedList<String> sortAndSplitFile(ByteBuffer buffer, FileChannel fileChannel) throws IOException {
		buffer.flip();
		int filled = buffer.limit();
		// 缓冲区的大小必须为4的倍数，这样保证我们可以读取到完整的整数
		int[] arr = new int[bufferSize / 4];

		int fileIndex = 0;
		long spiltStartTime = System.currentTimeMillis();
		LinkedList<String> files = new LinkedList<>();

		while( filled > 0){

			long start = System.currentTimeMillis();

			int pos = fillArray(arr,0,buffer);
			Arrays.sort(arr, 0, pos);
			String filePath = "sort." + fileIndex;
			writeIntArrayToFile(arr, 0, pos, filePath);

			System.out.println("sort." + fileIndex +"->分割并排序文件耗时:" + (System.currentTimeMillis() - start) + "ms");

			files.add(filePath);
			if(pos < arr.length) {
				//文件已读写完毕!
				break;
			}

			buffer.clear();
			filled = fileChannel.read(buffer);
			buffer.flip();
			fileIndex++;
		}
		System.out.println("分割完毕,总计耗时=" + (System.currentTimeMillis() - spiltStartTime) + "ms");
		return files;
	}

	private enum InsertState {
		EMPTY_A, EMPTY_B, WRITE_A, WRITE_B, COMPARE, END;
	}


	private static void merge(LinkedList<String> files) throws FileNotFoundException {
		int mergeIndex = 0;
		mainloop: while (files.size() != 1){

			long startMerge = System.currentTimeMillis();

			File aFile = new File(files.pop());
			File bFile = new File(files.pop());

			File cFile = new File("merge." + mergeIndex);

			mergeIndex++;

			FileInputStream inA = new FileInputStream(aFile);
			FileInputStream inB = new FileInputStream(bFile);

			FileOutputStream outC = new FileOutputStream(cFile);

			try {
				InsertState state = InsertState.COMPARE;
				int a = readInt(inA);
				if(reachEnd){
					state = InsertState.EMPTY_A;
				}

				reachEnd = false;

				int b = readInt(inB);
				if(reachEnd){
					if( state == InsertState.EMPTY_A) {
						reachEnd = false;
						System.out.println("两个文件均为空文件!");
						continue;
					}else{
						state = InsertState.EMPTY_B;
					}
				}

				for(;;) {
					switch (state) {
						case COMPARE: {
							if( a < b){
								state = InsertState.WRITE_A;
							}else {
								state = InsertState.WRITE_B;
							}
							break;
						}
						case EMPTY_A: {
							writeInt(outC, b);
							for( b = readInt(inB); !reachEnd; ){
								writeInt(outC, b);
								b = readInt(inB);
							}
							state = InsertState.END;
							break;
						}
						case EMPTY_B: {
							writeInt(outC, a);
							for ( a = readInt(inA); !reachEnd; ){
								writeInt(outC, a);
								a = readInt(inA);
							}
							state = InsertState.END;
							break;
						}
						case WRITE_A: {
							writeInt(outC,a);
							a = readInt(inA);
							if(reachEnd) {
								state = InsertState.EMPTY_A;
							}else {
								state = InsertState.COMPARE;
							}
							break;
						}
						case WRITE_B: {
							writeInt(outC,b);
							b = readInt(inB);
							if(reachEnd) {
								state = InsertState.EMPTY_B;
							}else {
								state = InsertState.COMPARE;
							}
							break;
						}
						case END: {
							//释放资源，继续下一次循环
							inA.close();
							inB.close();
							outC.close();
							files.add(cFile.getPath());
							reachEnd = false;
							System.out.println("merge:"+ cFile.getName()+ "-->timecost:" + (System.currentTimeMillis() - startMerge) + "ms");
							continue mainloop;
						}
						default: {
							break;
						}
					}
				}
			} catch (IOException e) {
				System.out.println("读取文件时发生错误!,错误信息:" + e.getMessage());
			}
		}
		System.out.println("合并完成!,目标文件:" + files.pop());
	}

	private static boolean reachEnd = false;

	private static int readInt(InputStream in) throws IOException {
		reachEnd = false;
		byte[] intBytes = new byte[4];

	 	if( in.read(intBytes) == -1 ){
	 		reachEnd = true;
	 		return 0;
		}
		return ((intBytes[0] & 0xff) << 24) | ((intBytes[1] & 0xff) << 16) | ( (intBytes[2] & 0xff) << 8) | (intBytes[3] & 0xff);
	}

	private static void writeInt(OutputStream out,int num) throws IOException {
		byte[] intBytes = new byte[4];
		intBytes[0] = (byte) (num >>> 24);
		intBytes[1] = (byte) (num >>> 16);
		intBytes[2] = (byte) (num >>> 8);
		intBytes[3] = (byte) num;
		out.write(intBytes);
	}



	public static int fillArray(int[] arr,int startPos, ByteBuffer buffer){
	 	// 每个 int 为4个字节, 数据文件必须符合需求即文件的长度必须为4的倍数
		int i = startPos;
		while ( buffer.hasRemaining() && buffer.limit() % 4 == 0) {

			byte b1 = buffer.get();
			byte b2 = buffer.get();
			byte b3 = buffer.get();
			byte b4 = buffer.get();
			arr[i] = ((b1 & 0xff) << 24) | ( (b2&0xff) << 16) | ( (b3&0xff) << 8) | (b4 & 0xff) ;
			i++;
		}
		return i;
	}

	public static void writeIntArrayToFile(int[] arr,int start,int end,String filePath) {
		try {
			OutputStream outputStream = new FileOutputStream(new File(filePath));
			byte[] intBytes = new byte[4];

			for(int i = start ; i < end; i++){
				int num = arr[i];
				intBytes[0] = (byte) (num >>> 24);
				intBytes[1] = (byte) (num >>> 16);
				intBytes[2] = (byte) (num >>> 8);
				intBytes[3] = (byte) num;
				outputStream.write(intBytes);
			}

			outputStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("输出文件时发生错误,错误信息:" + e.getMessage());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
