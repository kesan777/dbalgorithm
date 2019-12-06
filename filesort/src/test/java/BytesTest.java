import org.junit.Test;

public class BytesTest {

	@Test
	public void test() {
		int a = 0x0000AB00;
		byte b1 = (byte) 0xAB;

		int i1 = b1;

		System.out.println(Integer.toBinaryString(i1));

		System.out.println(Integer.toBinaryString(b1));
		// 左移最高位补1
		System.out.println(Integer.toBinaryString(b1 << 8));
		System.out.println(Integer.toBinaryString((b1 & 0xff) << 8));
		System.out.println(Integer.toBinaryString( b1 & 0xff));
	}
}
