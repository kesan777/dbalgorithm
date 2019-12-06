
public class ProduceDataFile {

	public static void main(String[] args){
		// 输出整数文件,无分隔符 每个整数按照四个字节来存取
		String filePath = "a.out";
		int size = 1024;
		int[] arr = new int[size];
		for(int i = 0 ; i < size ; i++){
			arr[i] = (int) (Math.random() * Integer.MAX_VALUE);
		}
		FileSortMain.writeIntArrayToFile(arr,0, size, filePath);
	}
}
