package compulinux.jpage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import  com.compulinux.jnhp.Jnhp;
import com.compulinux.jnhp.runApplication;

public class JPage extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static  transient Logger logger = Logger.getLogger(JPage.class);

	/**
	 * The package (java) to search for future clases. 
	 * 
	 */
	public static final String jnhpPackage = "ccb.demo.";

	public void destroy() {
		super.destroy();
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		return;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		
		PrintWriter out = null;
		response.setContentType("text/html");
		Jnhp.setPathFileName(getServletContext().getRealPath("/"));
		final String default_page = "bienvenidaprincipal";
		String page = request.getParameter("page");

		Jnhp.setPackageApplicationName(jnhpPackage);
		// Sesion check
		if (page == null) {
			page = default_page;
			String url = request.getContextPath() + "/JPage" + "?page=" + page;

			response.sendRedirect(url);
			return;
		}
		try {
			String pageClassName = jnhpPackage + page;
			//File filej;
			Jnhp j;
			try {
				//filej = new File(Jnhp.getPathFileName() + "/" + page + ".html");
				String myTopApp = "{{application id="+page+"}}";
				j = new Jnhp(myTopApp);
				j.setRequest(request);
				j.setResponse(response);
				try {
					Class t = Class.forName(pageClassName);
					runApplication o = (runApplication) t.newInstance();
					o.runApp(j);
				} catch (ClassNotFoundException e) {
					// No class found, no problem,HTML static !
					logger
							.info("Jnhp Page class "
									+ pageClassName
									+ " Not Found. Ignore this warning if you are trying with static HTML");
				} catch (NullPointerException e) {
					StringWriter sr = new StringWriter(0);
					PrintWriter pw = new PrintWriter(sr, true);
					e.printStackTrace(pw);
					if (out == null) {
						out = response.getWriter();
					}
					out.println("<pre> Jnhp : Exception :  " + sr.toString()
							+ "</pre>");
					return;
				}
				String xhtml  = j.toString();
				if (! response.isCommitted()) {
					out = response.getWriter();
					out.print(xhtml);
				}
			} catch (NullPointerException e) {
				if (out == null) {
					out = response.getWriter();
				}
				out
						.println("<font color=BLUE> Error JPage : P&aacute;gina  </font>"
								+ page
								+ " <font color=BLUE>No encontrado</font> ");
			}
		} catch (Exception e) {
			StringWriter sr = new StringWriter(0);
			PrintWriter pw = new PrintWriter(sr, true);
			logger.warn(e);
			e.printStackTrace(pw);
			if (!response.isCommitted()) {
				if (out == null) {
					out = response.getWriter();
				}
				out.println("<pre> Jnhp : Exception :  " + sr.toString()
						+ "</pre>");
			} else {
				logger.error(e);
			}
		}
	}

	/**
	 * The same as doGet. 
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doGet(request, response);
	}
}
