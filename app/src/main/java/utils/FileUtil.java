package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * 文件相关辅助类
 * 
 * @author panyi
 * 
 */
public class FileUtil {
	public static final String FOLDER_NAME = "GomoTest";

	/** 分隔符. */
	public final static String FILE_EXTENSION_SEPARATOR = ".";

	/**
	 * 获取存贮文件的文件夹路径
	 * 
	 * @return
	 */
	public static File createFolders() {
		File baseDir;
		if (android.os.Build.VERSION.SDK_INT < 8) {
			baseDir = Environment.getExternalStorageDirectory();
		} else {
			baseDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		}
		if (baseDir == null)
			return Environment.getExternalStorageDirectory();
		File aviaryFolder = new File(baseDir, FOLDER_NAME);
		if (aviaryFolder.exists())
			return aviaryFolder;
		if (aviaryFolder.isFile())
			aviaryFolder.delete();
		if (aviaryFolder.mkdirs())
			return aviaryFolder;
		return Environment.getExternalStorageDirectory();
	}

	public static File genEditFile(){
		return FileUtil.getEmptyFile("tietu"
				+ System.currentTimeMillis() + ".jpg");
	}

	public static File getEmptyFile(String name) {
		File folder = FileUtil.createFolders();
		if (folder != null) {
			if (folder.exists()) {
				File file = new File(folder, name);
				return file;
			}
		}
		return null;
	}

	/**
	 * 删除指定文件
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteFileNoThrow(String path) {
		File file;
		try {
			file = new File(path);
		} catch (NullPointerException e) {
			return false;
		}

		if (file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 保存图片
	 * 
	 * @param bitName
	 * @param mBitmap
	 */
	public static String saveBitmap(String bitName, Bitmap mBitmap) {
		File baseFolder = createFolders();
		File f = new File(baseFolder.getAbsolutePath(), bitName);
		FileOutputStream fOut = null;
		try {
			f.createNewFile();
			fOut = new FileOutputStream(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return f.getAbsolutePath();
	}

	// 获取文件夹大小
	public static long getFolderSize(File file) throws Exception {
		long size = 0;
		try {
			File[] fileList = file.listFiles();
			for (int i = 0; i < fileList.length; i++) { // 如果下面还有文件
				if (fileList[i].isDirectory()) {
					size = size + getFolderSize(fileList[i]);
				} else {
					size = size + fileList[i].length();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	/** * 格式化单位 * * @param size * @return */
	public static String getFormatSize(double size) {
		double kiloByte = size / 1024d;
		int megaByte = (int) (kiloByte / 1024d);
		return megaByte + "MB";
	}

	/**
	 * 
	 * @Description:
	 * @Author 11120500
	 * @Date 2013-4-25
	 */
	public static boolean isConnect(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	/**
	 * 判断SD卡是否可用
	 * @return SD卡可用返回true
	 */
	public static boolean hasSdcard() {
		String status = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(status);
	}


	/**
	 * 删除指定目录中特定的文件
	 * @param dir
	 * @param filter
	 */
	public static void delete(String dir, FilenameFilter filter) {
		if (TextUtils.isEmpty(dir))
			return;
		File file = new File(dir);
		if (!file.exists())
			return;
		if (file.isFile())
			file.delete();
		if (!file.isDirectory())
			return;

		File[] lists = null;
		if (filter != null)
			lists = file.listFiles(filter);
		else
			lists = file.listFiles();

		if (lists == null)
			return;
		for (File f : lists) {
			if (f.isFile()) {
				f.delete();
			}
		}
	}



	/**
	 * 获得不带扩展名的文件名称
	 * @param filePath 文件路径
	 * @return
	 */
	public static String getFileNameWithoutExtension(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			return filePath;
		}
		int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
		int filePosi = filePath.lastIndexOf(File.separator);
		if (filePosi == -1) {
			return (extenPosi == -1 ? filePath : filePath.substring(0,
					extenPosi));
		}
		if (extenPosi == -1) {
			return filePath.substring(filePosi + 1);
		}
		return (filePosi < extenPosi ? filePath.substring(filePosi + 1,
				extenPosi) : filePath.substring(filePosi + 1));
	}

}// end
