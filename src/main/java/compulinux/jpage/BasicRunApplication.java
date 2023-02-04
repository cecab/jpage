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

	/** Restringe los controles sobre los que se va a trabajar */
	public void defineHTMLValidWidgets(String widgets[]) {
		this.misWidgets = new ArrayList();
		for (int j = 0; j < widgets.length; j++) {
			this.misWidgets.add(widgets[j]);
		}
	}

	/**
	 * TRUE si el control se considera como "mio". Si la lista misWidgets esta
	 * vacia siempre retorna TRUE.
	 */
	public boolean isMiWidget(String wname) {
		boolean ret = true;
		if (this.misWidgets != null) {
			if (this.misWidgets.size() != 0) {
				for (int k = 0; k < misWidgets.size(); k++) {
					String wn = (String) misWidgets.get(k);
					if (wn.compareTo(wname) == 0) {
						return true;
					}
				}
				ret = false;
			}
		}
		return ret;
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

	/**
	 * Check in the list of "HTML widgets" for the page if the name (argument)
	 * exist return true if the HTML widget exist, otherwise returns false.
	 */

	public boolean checkExistHTMLWidget(String wdName,
			HttpServletRequest request) {
		HttpSession session = request.getSession();
		if (getPageID(request) == null) {
			return false;
		}
		Object sp = session.getAttribute("sess" + getPageID(request));
		if (sp == null) {
			return false;
		}
		Hashtable form = (Hashtable) sp;
		Enumeration variables = form.keys();
		while (variables.hasMoreElements()) {
			String keyname = (String) variables.nextElement();
			if (keyname.equals(wdName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Restitute values from HTTPSession, using a pre-defined naming convention
	 * for HTML elements, this method will set saved values for HTML: - INPUT
	 * Text tag. Inside the session, this method looks for the key
	 * "sess"+getPageID(request) if this key returns an object, it's casted as
	 * an ArrayList of Hashtable, in which element has the following properties:
	 * "name" ==> name attribute of the HTML element. It should be n__xxxx in
	 * the HTML page to be picked up by this parser "value" ==> value attribute
	 * of the HTML element It should be v__xxxx
	 */
	public void getStatePage(Jnhp html, HttpServletRequest request)
			throws Exception {
		HttpSession session = request.getSession();
		// logger.debug("iniside getStatePage with page=" + getPageID(request));
		if (getPageID(request) == null) {
			throw new Exception("Page attribute not found in request");
		}
		Object sp = session.getAttribute("sess" + getPageID(request));
		if (sp == null) {
			// logger.debug("NULL in session. Initialize the Page in session");
			session.setAttribute("sess" + getPageID(request), new Hashtable());
			return;
		}
		Hashtable form = (Hashtable) sp;
		Enumeration variables = form.keys();
		while (variables.hasMoreElements()) {
			String keyname = (String) variables.nextElement();
			String vname = (String) ((Hashtable) form.get(keyname)).get("name");
			String vvalue = (String) ((Hashtable) form.get(keyname))
					.get("value");
//			 logger.debug("vname="+vname+", value="+vvalue);
			if (Pattern.matches("^c__(.*)$", vname)) {
				// This is a checkbox, there should be an "c__xxx" to replace
				// the state.
				Pattern p = Pattern.compile("^c__(.*)$");
				Matcher m = p.matcher(vname);
				m.matches();
				String bname = m.group(1);
				if (((Boolean) ((Hashtable) form.get(keyname)).get("state")).booleanValue()) {
//					logger.debug("setVar for checkbox " + bname + " CHECKED");
					html.setVar("s__" + bname, "CHECKED");
				} else {
					html.setVar("s__" + bname, "");
				}
				logger.debug("val" + vname + "---bname>" + bname+"---state"+((Hashtable) form.get(keyname)).get("state"));
				// The v__xxx ID is optional
				try {
					html.setVar(vname.replaceAll("c__", "v__"), vvalue);
				} catch (Exception e) {
					logger.warn("Check box " + vname
							+ " without VALUE attribute ");
				}
			}
			// this block is for <INPUT type="RADIO" elements
			if (Pattern.matches("^r__(.*)$", vname)) {
				// logger.debug("RADIO detected:" + vname ) ;
				Pattern p = Pattern.compile("^r__(.*)$");
				Matcher m = p.matcher(vname);
				m.matches();
				String bname = m.group(1);
				ArrayList choices = (ArrayList) ((Hashtable) form.get(keyname))
						.get("choices");
				// logger.debug(choices.size() + " choices for RADIO "+ vname );
				for (int mx = 0; mx < choices.size(); mx++) {
					String vchoice = (String) choices.get(mx);
					// logger.debug("choice = " + vchoice) ;
					// html.setVar("r__"+bname+"_"+(mx+1), vchoice);
					if (vchoice.equals(vvalue)) {
						// logger.debug("checking choice " + vvalue );
						// logger.debug("SETVAR rs__"+bname+"_"+(mx+1)+"
						// CHECKED");
						html.setVar("rs__" + bname + "_" + (mx + 1), "CHECKED");
					}
				}
			}
			// this block is for <SELECT elements
			if (Pattern.matches("^s__(.*)$", vname)) {
				// logger.debug("COMBO detected:" + vname ) ;
				Pattern p = Pattern.compile("^s__(.*)$");
				Matcher m = p.matcher(vname);
				m.matches();
				String bname = m.group(1);
				ArrayList options = (ArrayList) ((Hashtable) form.get(keyname))
						.get("options");
				// logger.debug(options.size() + " OPCIONES para " + vname );
				for (int mx = 0; mx < options.size(); mx++) {
					String otext = (String) ((Hashtable) options.get(mx))
							.get("text");
					String ovalue = (String) ((Hashtable) options.get(mx))
							.get("value");
					html.setVar("vt__" + bname, otext);
					html.setVar("v__" + bname, ovalue);
					if (ovalue.equals(vvalue)) {
						html.setVar("sc__" + bname, "SELECTED");
					}
					// logger.debug("REPEATING s__"+bname);
					html.doRepeat("s__" + bname);
				}
			}
			if (Pattern.matches("^n__(.*)$", vname)) {
				html.setVar(vname.replaceAll("n__", "v__"), vvalue);
			}
		}
	}

	/**
	 * cleanStatePage(request): erase the information of HTML componentes from
	 * the session. This is equivalent to "reset" an HTML form.
	 */
	public void cleanStatePage(HttpServletRequest request) {

		HttpSession session = request.getSession();
		// logger.debug("CLEANING session from page=" + getPageID(request));
		session.removeAttribute("sess" + getPageID(request));
	}

	/**
	 * cleanStatePage: erase all the informaction associated with page indicated
	 * by the second argument
	 */
	public void cleanStatePage(HttpServletRequest request, String page) {

		HttpSession session = request.getSession();
		// logger.debug("CLEANING session from page=" +page);
		session.removeAttribute("sess" + page);
	}


    /** Obtiene los valores de un select multiple
     */

    public ArrayList getHTMLValues(String vname, HttpServletRequest request) throws PresentationLayerException {
        HttpSession  session = request.getSession() ;
        Hashtable sp = (Hashtable) session.getAttribute("sess"+ request.getParameter("page")) ;
        if ( sp == null ) {
                logger.debug("Session no tiene page .. returning NULL");
            return null ;
        }
        if ( sp.get(vname)  == null ) {
            // El elemento HTML no existe en el "formulario"
            logger.debug("Elemento HTML " + vname + " no encontrado en Formulario " + request.getParameter("page"));
            return null ;
        }
        Hashtable htmlElement = ( Hashtable) sp.get(vname);
        // For checkboxes, they have values and states, we need the state ON/OFF as the
        // result  for this method. The result will be NULL for checkboxes in OFF state
        if ( Pattern.matches("^c__(.*)$",vname) ) {
            boolean estado =  ((Boolean) htmlElement.get("state")).booleanValue();
            if ( ! estado ) {
                return null ;
            }
        }
        return (ArrayList) htmlElement.get("values");
    }

	/**
	 * Obtiene el valor de un Widget HTML. 
	 * @return El valor, o NULL si el KEY de la variable no es parte del 
	 *  formulario
	 */

	public String getHTMLValue(String vname, HttpServletRequest request)
			throws PresentationLayerException {
		HttpSession session = request.getSession();
		Hashtable sp = (Hashtable) session.getAttribute("sess"
				+ getPageID(request));
		if (sp == null) {
			logger.debug("Session no tiene page .. returning NULL");
			return null;
		}
		if (sp.get(vname) == null) {
			// El elemento HTML no existe en el "formulario"
			logger.debug("Elemento HTML " + vname
					+ " no encontrado en Formulario " + getPageID(request));
			return null;
		}
		Hashtable htmlElement = (Hashtable) sp.get(vname);
		// For checkboxes, they have values and states, we need the state ON/OFF
		// as the
		// result for this method. The result will be NULL for checkboxes in OFF
		// state
		if (Pattern.matches("^c__(.*)$", vname)) {
			boolean estado = ((Boolean) htmlElement.get("state"))
					.booleanValue();
			if (!estado) {
				return null;
			}
		}
		return (String) htmlElement.get("value");
	}

	/**
	 * Setting values for Widgets that a boolean make sense. A checkbox is a
	 * good example, with this method, you can pass a boolean as the value, and
	 * it will be interpreted as a CHECK for TRUE and NO-CHECK for FALSE.
	 */
	public void setHTMLValue(String vname, boolean vvalue,
			HttpServletRequest request) {
		HttpSession session = request.getSession();
		Hashtable sp = (Hashtable) session.getAttribute("sess"
				+ getPageID(request));
		Hashtable htmlElement = (Hashtable) sp.get(vname);
		if (htmlElement == null) {
			htmlElement = new Hashtable();
		}
		htmlElement.put("name", vname);
		htmlElement.put("state", new Boolean(vvalue));
		htmlElement.put("value", "");
		sp.put(vname, htmlElement);
		// logger.debug("htmlElement=" + htmlElement + ", vvalue = " + vvalue );
	}

	public void setHTMLValue(String vname, String vvalue,
			HttpServletRequest request) {
		// logger.debug("setHTMLValue:: var="+vname+", value="+vvalue);
		if (isHTMLButton(vname)) {
			setHTMLButton(vname);
			return;
		}

		HttpSession session = request.getSession();
		Hashtable sp = (Hashtable) session.getAttribute("sess"
				+ getPageID(request));
		if (sp == null) {
			// logger.debug("NULL in session. Initialize the Page in session");
			sp = new Hashtable();
			session.setAttribute("sess" + getPageID(request), sp);

		}
		if (vvalue == null) {
			logger.warn("setHTMLValue received NULL value. ");
		}

		// Parsing of <input type="text" ../>
		if (Pattern.matches("^n__(.*)$", vname)) {
			// save the pair name,value into the hashtable
			Hashtable onePair = new Hashtable();
			onePair.put("name", vname);
			if (vvalue == null) {
				vvalue = "";
			}
			onePair.put("value", vvalue);
			logger.debug("putting in Session "+vname+"="+vvalue);
			sp.put(vname, onePair);
		}
		// Parsing of <input type="checkbox" name="c__xxxx"... />
		// usar s__xxx as a reference for the state of the checkbox
		// which will be replaced with "CHECKED" or "" depending of
		// the state (true|false)
		if (Pattern.matches("^c__(.*)$", vname)) {
			// save the triple name,value,state into the hashtable
			Hashtable onePair = new Hashtable();
			onePair.put("name", vname);
			if (vvalue == null) {
				onePair.put("state", new Boolean(false));
			} else {
				onePair.put("value", vvalue);
				onePair.put("state", new Boolean(true));
			}
			// logger.debug("putting in Session Checkbox: "+vname+"="+vvalue);
			sp.put(vname, onePair);
		}
		/**
		 * Parsing of <SELECT name="s__xxxx">{{repeat id=s_xxxx}}<option
		 * value="v_xxx">{{vt_xxxx}}></option> {{/repeat}} </SELECT>
		 * 
		 * Note the convention for naming: s_xxxx: Is the name of HTML element.
		 * s_xxxx: the same, is the Repeat Tag to loop over multiples values.
		 * vt_xxxx: The text used to show alternatives to the user v_xxxx: the
		 * value for each option.
		 */

		if (Pattern.matches("^s__(.*)$", vname)) {
			// Note for this element that we receive the Value, but maybe
			// we already have the options, which must be preserved.
			Hashtable onePair;
			if ((onePair = (Hashtable) sp.get(vname)) == null) {
				onePair = new Hashtable();
				onePair.put("options", new ArrayList());
			}
			onePair.put("name", vname);
			if (vvalue == null) {
				vvalue = "";
			}
			onePair.put("value", vvalue);
			// This field (options) will be filled with the method
			// addItemHTMLSelect(String textOption, String valueOption)
			sp.put(vname, onePair);

		}
		if (Pattern.matches("^r__(.*)$", vname)) {
			// Note for this element that we receive the Value, but maybe
			// we already have the options, which must be preserved.
			Hashtable onePair;
			if ((onePair = (Hashtable) sp.get(vname)) == null) {
				onePair = new Hashtable();
				onePair.put("choices", new ArrayList());
			}
			onePair.put("name", vname);
			onePair.put("value", vvalue);
			// This field (options) will be filled with the method
			// addItemHTMLSelect(String textOption, String valueOption)
			sp.put(vname, onePair);
			// dump choices for RADIO
			ArrayList choices = (ArrayList) ((Hashtable) sp.get(vname))
					.get("choices");
			for (int jz = 0; jz < choices.size(); jz++) {
				logger.debug("Dumping choice" + jz + "="
						+ (String) choices.get(jz));
			}

		}
		// FIXME: las variables tipo FILE upload se guardan en un Hash distinto.
		// if ( Pattern.matches("^[bf]__(.*)$",vname) ) {
		//	
		// sp.put(vname,vvalue);
		// }
		// finally: IF variable doesn't match with any prefix et all .. we
		// still save it, so we can use getHTMLValue transparently . This is the
		// por "_event_" var .. which allow as to hande javascript events.
		// Update into session
		if (!Pattern.matches("^[a-z]__(.*)$", vname)) {
			Hashtable onePair = new Hashtable();
			onePair.put("name", vname);
			onePair.put("value", vvalue);
			logger.info("Putting vname="+vname + ",value="+ vvalue);
			sp.put(vname, onePair);

		}
		session.setAttribute("sess" + getPageID(request), sp);
	}

	public static final String MULTIPART_PREFIX = "sess_multipart_";

	/**
	 * Mantiene el objeto de session para los campos que son de tipo muiltipart,
	 * es decir aquellos usados en los procesos de UPLOAD.
	 * 
	 * @param request
	 * @return Retorna el Hash con las variables obtenidas de un request de tipo
	 *         Multipart.
	 */
	public Hashtable getOrCreateMultipart(HttpServletRequest request) {
		HttpSession session = request.getSession();
		// logger.debug("Checking MULTIPART data in page=" +
		// getPageID(request));
		if (getPageID(request) == null) {
			throw new PresentationLayerException(
					"Page attribute not found in request");
		}
		Hashtable sp;
		String keysess = MULTIPART_PREFIX + getPageID(request);
		// Check if we have some state in session, if not create a new Hashtable
		if (session.getAttribute(keysess) == null) {
			sp = new Hashtable();
			session.setAttribute(keysess, sp);
		}
		sp = (Hashtable) session.getAttribute(keysess);

		return sp;
	}

	/**
	 * Obtiene el objeto de tipo File-Item para un formulario multipart.
	 * 
	 * @param fieldName
	 *            El nombre de la variable como fue definida en el formulario
	 *            HTML
	 * @return El objeto de tipo FileItem
	 */
	public FileItem getMultiPartItem(String fieldName,
			HttpServletRequest request) {
		Hashtable ht = getOrCreateMultipart(request);
		if (ht.get(fieldName) == null) {
			return null;
		}
		return (FileItem) ((Hashtable) ht.get(fieldName)).get("value");
	}

	/**
	 * Obtiene el identificados para guardar los datos en sesion asociados a
	 * esta pagina.
	 */
	private String getPageID(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String ret = null;
		// En esta implementacion NO USAMOS la variable "page" del requets, pues
		// es limitado para caso con multiples applications en una misma pagina.
		String partes[] = this.getClass().getName().split("\\.");
		ret = partes[partes.length - 1];
		return ret;

	}

	/**
	 * setStatePage(request): parse the variable from request and save them if
	 * they follow the naming rules n__xxx for variable names
	 */
	public void setStatePage(HttpServletRequest request) throws Exception {
		initHTMLButtons(request);
		HttpSession session = request.getSession();

		logger.debug(this.getClass().getName());
		if (getPageID(request) == null) {
			throw new Exception("Page attribute not found in request");
		}
		Hashtable sp;
		// Check if we have some state in session, if not create a new Hashtable
		if (session.getAttribute("sess" + getPageID(request)) == null) {
			sp = new Hashtable();
		} else {
			sp = (Hashtable) session.getAttribute("sess" + getPageID(request));
		}

		List fileItems = null;
		// Evaluar el tipo de formulario. Para el caso de multipart/ se usara
		// el API de commons-fileupload de Jakarta.
		if (request.getMethod().equals("POST")
				&& Pattern.matches("^multipart/form-data;.*", request
						.getContentType())) {
			DiskFileUpload fu = new DiskFileUpload();
			fu.setSizeMax(-1); // ilimitado
			fileItems = fu.parseRequest(request);

			// Iteramos por cada dato
			Iterator itrf = fileItems.iterator();
			while (itrf.hasNext()) {
				FileItem actual = (FileItem) itrf.next();
				String vname = actual.getFieldName();
				String vvalue = null;
				if (actual.isFormField()) {
					vvalue = actual.getString();
					setHTMLValue(vname, vvalue, request);
					logger.debug("setPageState " + vname + "=" + vvalue);
				} else {
					// Este caso se trata de un archivo UPLOADED. Manejarlo en
					// Hash alternativo, pues su tratamiento es totalmente
					// diferente.
					logger.warn("FORM field content-type:"
							+ actual.getContentType() + " vname=" + vname
							+ " vvalue " + vvalue);
					Hashtable onePair = new Hashtable();
					onePair.put("name", vname);
					onePair.put("value", actual);
					(getOrCreateMultipart(request)).put(vname, onePair);

				}
			}
		} else {
			Enumeration vnames = request.getParameterNames();

			// FIXME: This code can't handle variables with multiples values
			while (vnames.hasMoreElements()) {
				String vname = (String) vnames.nextElement();
				String vvalue = request.getParameter(vname);
				if (!isMiWidget(vname)) {
					continue;
				}
				setHTMLValue(vname, vvalue, request);
			}
		}

		// For checkboxes that exist in Session but they are not been set
		// in this request.. we have to change their state to FALSE, because
		// this is interpreted as an Uncheck action in the browser
		if (request.getMethod().equals("POST")
				&& Pattern.matches("^multipart/form-data;.*", request
						.getContentType())) {

			Enumeration keys = sp.keys();
			while (keys.hasMoreElements()) {
				Hashtable htmlElement = (Hashtable) sp.get(keys.nextElement());
				String htmlElementName = (String) htmlElement.get("name");
//				logger.debug("htmlElement:" + htmlElementName);
				if (!Pattern.matches("^c__(.*)$", htmlElementName)) {
					continue;
				}
				boolean flag_found = false;
				Iterator itrf = fileItems.iterator();
				while (itrf.hasNext()) {
					FileItem actual = (FileItem) itrf.next();
					String vname = actual.getFieldName();
					if (htmlElementName.equals(vname)) {
						flag_found = true;
						break;
					}
				}
				if (!flag_found) {
					// Change state to UNCHECK.
					logger.debug("UNCHECKING " + htmlElementName
							+ " checkbox .. ");
					htmlElement.put("state", new Boolean(false));
				} else {
					htmlElement.put("state", new Boolean(true));
				}
			}
		} else {
			Enumeration keys = sp.keys();
			Hashtable htmlElement = null;
			while (keys.hasMoreElements()) {
				htmlElement = (Hashtable) sp.get(keys.nextElement());
				String htmlElementName = (String) htmlElement.get("name");
//				logger.debug("htmlElement:" + htmlElementName);
				if (!Pattern.matches("^c__(.*)$", htmlElementName)) {
					continue;
				}
				Enumeration qnames = request.getParameterNames();
				boolean flag_found = false;
				while (qnames.hasMoreElements()) {
					String htmlVar = (String) qnames.nextElement();
//					logger.debug("Comparing with "+htmlVar);
					if (htmlElementName.equals(htmlVar)) {
						flag_found = true;
						logger.debug("TRUE "+htmlVar+"-"+htmlElementName);
						break;
					}
				}
				if (!flag_found) {
					// Change state to UNCHECK.
					logger.debug("UNCHECKING " + htmlElementName
							+ " checkbox .. ");
					htmlElement.put("state", new Boolean(false));
				}else{
					htmlElement.put("state", new Boolean(true));
				}
				logger.debug(htmlElement);
			}
		}
		if (sp.size() != 0) {
			// logger.debug("There are " + sp.size() + " dynamic tags in page "
			// + getPageID(request));
			session.setAttribute("sess" + getPageID(request), sp);
		}
	}

	/**
	 * Retorna el numero de alternativas con que cuenta un arreglo de RADIO
	 */
	public int HTMLRadioSize(HttpServletRequest request, String vname) {
		HttpSession session = request.getSession();
		if (session.getAttribute("sess" + getPageID(request)) == null) {
			return 0;
		}
		Hashtable sp = (Hashtable) session.getAttribute("sess"
				+ getPageID(request));
		if (sp.get(vname) == null) {
			return 0;
		}
		Hashtable combo = (Hashtable) sp.get(vname);
		if (combo.get("choices") == null) {
			return 0;
		}
		ArrayList options = (ArrayList) combo.get("choices");
		return options.size();
	}

	/**
	 * HTMLSelectSize: return el numero de opciones con que cuenta un COMBO
	 * SELECT
	 */
	public int HTMLSelectSize(HttpServletRequest request, String vname) {
		HttpSession session = request.getSession();
		if (session.getAttribute("sess" + getPageID(request)) == null) {
			return 0;
		}
		Hashtable sp = (Hashtable) session.getAttribute("sess"
				+ getPageID(request));
		if (sp.get(vname) == null) {
			return 0;
		}
		Hashtable combo = (Hashtable) sp.get(vname);
		if (combo.get("options") == null) {
			return 0;
		}
		ArrayList options = (ArrayList) combo.get("options");
		return options.size();
	}

	/**
	 * Agrega posibles valores para un arreglo de RADIO.Estos radios deben ser
	 * escritos en el HTML como r__xxxx y sus respectivos valores y estados
	 * como: r__xxxx_1, rs__xxx_1| r__xxx_2,rs__xxx_2 | ...
	 */
	public void addItemHTMLRadio(HttpServletRequest request, String vname,
			String voption) throws Exception {
		// logger.debug("Adding value="+voption+", to radio" + vname ) ;
		HttpSession session = request.getSession();
		if (getPageID(request) == null) {
			logger.debug("Page attribute not found in request");
			throw new Exception("Page attribute not found in request");
		}
		Hashtable sp = null;
		if (session.getAttribute("sess" + getPageID(request)) == null) {
			logger.debug("NULL in session. Initialize the Page in session");
			sp = new Hashtable();
			session.setAttribute("sess" + getPageID(request), sp);
		} else {
			sp = (Hashtable) session.getAttribute("sess" + getPageID(request));
		}
		Hashtable radio = null;
		if ((radio = (Hashtable) sp.get(vname)) == null) {
			logger.debug("HTMLRadio element: " + vname + " Not found in page="
					+ getPageID(request) + ". CREANDOLO");
			radio = new Hashtable();
			radio.put("name", vname);
			logger.debug("addItemHTMLRadio::new choices ArrayList");
			radio.put("choices", new ArrayList());
		}
		ArrayList choices;
		choices = (ArrayList) radio.get("choices");
		choices.add(new String(voption));
		radio.put("choices", choices);
		sp.put(vname, radio);
		logger.debug("Setting sess" + getPageID(request));
		session.setAttribute("sess" + getPageID(request), sp);
		logger.debug("addItemHTMLRadio, size=" + HTMLRadioSize(request, vname));
	}

	public void clearHTMLSelect(HttpServletRequest request, String vname) {
		logger.debug("Clearing HTMLCombo " + vname);
		HttpSession session = request.getSession();
		if (getPageID(request) == null) {
			logger.debug("Page attribute not found in request");
			throw new PresentationLayerException(
					"Page attribute not found in request");
		}
		Hashtable sp = null;
		if (session.getAttribute("sess" + getPageID(request)) == null) {
			logger.debug("NULL in session. Initialize the Page in session");
			return;
		} else {
			sp = (Hashtable) session.getAttribute("sess" + getPageID(request));
		}
		Hashtable combo = null;
		if ((combo = (Hashtable) sp.get(vname)) == null) {
			logger.debug("HTMLSelect element: " + vname + "Not found in page="
					+ getPageID(request) + ". CREANDOLO");
			return;
		}
		// sp.put(vname,null);
		sp.remove(vname);
		logger.debug("Setting sess" + getPageID(request));
		session.setAttribute("sess" + getPageID(request), sp);
	}

	public void addItemHTMLSelect(HttpServletRequest request, String vname,
			String voption, String toption) {
		logger.debug("Adding value=" + voption + ", text=" + toption
				+ " to SELECT" + vname);
		HttpSession session = request.getSession();

		if (getPageID(request) == null) {
			logger.debug("Page attribute not found in request");
			throw new PresentationLayerException(
					"Page attribute not found in request");
		}
		Hashtable sp = null;
		if (session.getAttribute("sess" + getPageID(request)) == null) {
			logger.debug("NULL in session. Initialize the Page in session");
			sp = new Hashtable();
			session.setAttribute("sess" + getPageID(request), sp);
		} else {
			sp = (Hashtable) session.getAttribute("sess" + getPageID(request));
		}
		Hashtable combo = null;
		if ((combo = (Hashtable) sp.get(vname)) == null) {
			logger.debug("HTMLSelect element: " + vname + "Not found in page="
					+ getPageID(request) + ". CREANDOLO");
			combo = new Hashtable();
			combo.put("name", vname);
			combo.put("options", new ArrayList());
		}
		ArrayList options;
		options = (ArrayList) combo.get("options");
		Hashtable oneOption = new Hashtable();
		oneOption.put("value", voption);
		oneOption.put("text", toption);
		options.add(oneOption);
		combo.put("options", options);
		sp.put(vname, combo);
		logger.debug("Ya hay " + options.size() + " opciones en " + vname);
		session.setAttribute("sess" + getPageID(request), sp);
	}

	/** parse and modify html entries */
	public void updateStatePage(Jnhp html, HttpServletRequest request)
			throws Exception {
		logger.debug("setStatePage ...");
		setStatePage(request); // read from request .. create objects in the
		// sesion
		logger.debug("getStatePage ...");
		getStatePage(html, request);
	}

	/**
	 * Verifica si la variable corresponde a un BUTTON html, para ello compara
	 * su valor con el patron buttonname.[xy]
	 */
	protected boolean isHTMLButton(String vname) {
		Pattern p = Pattern.compile("(.*)\\.[xy]$");
		Matcher m = p.matcher(vname);
		return m.matches();
	}

	/**
	 * Inscribe el button en la sesion HTTP para poder responder al codigo del
	 * usuario getHTMLButton(), sin embargo este arreglo de botones no persiste
	 * mas halla del request.
	 */
	protected void setHTMLButton(String vname) {
		// Extraer solo el nombre del boton, si es que fuera de la forma
		// buttonName.[xy].
		String xname = vname;
		Pattern p = Pattern.compile("(.*)\\.[xy]$");
		Matcher m = p.matcher(vname);
		if (m.matches()) {
			xname = m.group(1);
		}
		HttpSession session = request.getSession();
		Hashtable ht = (Hashtable) session.getAttribute(KEY_SESSION_BUTTONS);
		ht.put(xname, new Boolean(true));
		logger.debug("Button " + vname + " pressed." + xname + " SAVED");
	}

	/**
	 * Inicializa el Hash de Session que mantiene el nombre de los botones
	 * recibidos en el request. Este hash se REINICA con cada llamada a
	 * setStatePage.
	 * 
	 */
	private void initHTMLButtons(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.setAttribute(KEY_SESSION_BUTTONS, new Hashtable());
		logger.debug("RESETING button array");
	}

	/**
	 * Revisa si el boton esta en el arreglo de botones presionados _-aunque
	 * normalmente solo se puede presionar un boton
	 * 
	 * @param buttonName
	 *            el nombre del boton segun el attribute "name=" del HTML
	 * @return true, si el boton fue presinado, FALSE de otra forma.
	 */
	public boolean isHTMLButtonPressed(String buttonName) {
		HttpSession session = request.getSession();
		Hashtable ht = (Hashtable) session.getAttribute(KEY_SESSION_BUTTONS);
		boolean ret = (ht.get(buttonName) != null);
		logger.debug("Button pressed " + buttonName + " ??" + ret);
		return ret;
	}
	/** Evalua el request para ver si se trata de un POST/Multipart. */
	protected boolean isMultiPart(HttpServletRequest request) {
		return  request.getMethod().equals("POST")
		&& Pattern.matches("^multipart/form-data;.*", request
				.getContentType());
	}
}
