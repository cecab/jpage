package compulinux.jpage;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.compulinux.jnhp.Jnhp;
import  com.compulinux.jnhp.runApplication;

/**
 * This class will implement the logic for the resources strings for security.
 * @author cesarcastillo
 *
 */
public abstract class AbstractRunApplication implements runApplication {
	public static Logger logger = Logger
			.getLogger(AbstractRunApplication.class);

	protected HttpServletRequest request;
	private String resourceName;
	private ArrayList resources;
	private HttpSession session;

	private static String getClassName(Class cl) {
		String name = cl.getName();
		int index = name.lastIndexOf('.');

		if (index != -1)
			name = name.substring(index + 1);

		return name;
	}

	/**
	 * It defauls the resource name as a String based on the class name.
	 */
	public AbstractRunApplication() {
		init(getClassName(getClass()), null);
	}

	/**
	 * 
	 * @param resourceName The resource name that identifies this application.
	 *            
	 * 
	 */
	public AbstractRunApplication(String resourceName) {
		init(resourceName, null);
	}

	/**
	 * Also, we can start with a list of resources. 
	 * 
	 * @param resources The list of resources. 
	 * 
	 */
	public AbstractRunApplication(String[] resources) {
		init(getClassName(getClass()), resources);
	}

	/**
	 * Constuctor that use the name for this page, and the list of 
	 * resources used to reinforce the security.
	 * @param resourceName The name for this page. 
	 * 
	 * @param resources The list of the resources. 
	 *            
	 */
	public AbstractRunApplication(String resourceName, String[] resources) {
		init(resourceName, resources);
	}

	private void init(String resourceName, String[] resources) {
		this.resourceName = resourceName;
		this.resources = new ArrayList();
		if (resources != null) {
			for (int z = 0; z < resources.length; z++) {
				this.resources.add(resources[z]);
			}
		}
	}
	/** 
	 * Start to process the page with security reinforcement. 
	 */
	public void runApp(Jnhp html) throws Exception {
		request = html.getRequest();
		HttpServletResponse response = html.getResponse();

		session = request.getSession(false);
		
		if (checkAccess(this.resourceName) ) {
			refreshPager(html.getRequest());
			logger.debug("access allowed");
			onRunApp(html);
		} 
		else {
			throw new RuntimeException("ACLException: No acceso to "
					+ resourceName + ", you do not have the access list");
		}
	}

	public void onAccessError(HttpServletResponse response, Jnhp html)
			throws Exception {
		response.sendRedirect("Jpage?page=accesserror");
	}

	public void onRunApp(Jnhp html) throws Exception {
		request = html.getRequest();
		HttpServletResponse response = html.getResponse();

		if (request.getMethod().compareTo("POST") == 0)
			doPost(request, response, html);
		else if (request.getMethod().compareTo("GET") == 0)
			doGet(request, response, html);
	}

	public abstract void doPost(HttpServletRequest request,
			HttpServletResponse response, Jnhp html) throws Exception;

	public abstract void doGet(HttpServletRequest request,
			HttpServletResponse response, Jnhp html) throws Exception;

	protected boolean checkAccess(String resource) {
		return ( this.resources.indexOf(this.resourceName) >= 0) ; 
	}

	public void refreshPager(HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (session.getAttribute(getPagerId()) == null) {
			return;
		}
		Object obj = session.getAttribute(getPagerId());
		Pager px = (Pager) session.getAttribute(getPagerId());
		px.processEvent(request);
	}

	/**
	 * Implementacion defaulta par identificar a un Pager asociado a una
	 * application
	 */
	public String getPagerId() {
		return "PAGER_DEFAULT_ID";
	}
}
