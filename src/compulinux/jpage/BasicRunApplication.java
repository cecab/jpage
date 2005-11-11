package compulinux.jpage;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import  com.compulinux.jnhp.Jnhp;

public class BasicRunApplication extends AbstractRunApplication {
	public static final String KEY_SESSION_BUTTONS = "_key_for_buttons__";

	public static  transient Logger logger = Logger.getLogger("presentation");

	private List misWidgets;

	/**
	 * Asume que el nombre del recurso para esta pantalla es el de la clase.
	 * additonal commetn from workbenth in Eclipse.
	 */
	public BasicRunApplication() {
		super();
	}

	/**
	 * @param resourceName
	 *            es el nuevo nombre a dar al recurso de esta aplicacion.
	 * 
	 */
	public BasicRunApplication(String resourceName) {
		super(resourceName);
	}

	/**
	 * Asume que el nombre del recurso para esta pantalla es el de la clase y
	 * ademas quiere comprobar los accesos a los resources.
	 * 
	 * @param resources
	 *            recursos que va acceder esta pantalla, y que se necesitan
	 *            comprobar.
	 * 
	 */
	public BasicRunApplication(String[] resources) {
		super(resources);
	}

	/**
	 * Se le da el nombre del recurso para esta pantalla y ademas quiere
	 * comprobar los accesos a los resources.
	 * 
	 * @param resourceName
	 *            nombre de recurso de esta pantalla.
	 * 
	 * @param resources
	 *            recursos que va acceder esta pantalla, y que se necesitan
	 *            comprobar.
	 * 
	 */
	public BasicRunApplication(String resourceName, String[] resources) {
		super(resourceName, resources);
	}

	public void doPost(HttpServletRequest request,
			HttpServletResponse response, Jnhp html) throws Exception {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response,
			Jnhp html) throws Exception {
	}

}
