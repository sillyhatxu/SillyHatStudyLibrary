package start;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.util.resource.Resource;

public class TestURL {

	public static void main(String[] args) throws IOException {
//		boolean useCaches = true;
//		String resource = "src/main/webapp";
//		File file = new File(resource).getCanonicalFile();
//		System.out.println(file);
//		URL url = Resource.toURL(file);
//		URLConnection connection = url.openConnection();
//		connection.setUseCaches(useCaches);
//		Resource test =  new FileResource(url, connection, file);
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("a", "aaaa");
		result.put("b", "bbbbb");
		result.put("c", "cccc");
		result.put("d", "ddd");
		result.put("e", "eee");
		//取得根目录路径
		String rootPath=TestURL.class.getResource("/").getFile().toString();
		//当前目录路径
		String currentPath1=TestURL.class.getResource(".").getFile().toString();
		String currentPath2=TestURL.class.getResource("").getFile().toString();
		//当前目录的上级目录路径
		String parentPath=TestURL.class.getResource("../").getFile().toString();
		System.out.println(rootPath);
		System.out.println(currentPath1);
		System.out.println(currentPath2);
		System.out.println(parentPath);
		System.out.println(System.getProperty("user.dir"));
	}
}
