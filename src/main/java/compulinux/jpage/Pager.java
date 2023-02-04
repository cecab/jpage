package compulinux.jpage ;
import org.apache.log4j.* ;

import javax.servlet.http.*;
import java.util.* ;
/** Clase para paginar elementos de tipo arrays. Como los resportes
 */
public class Pager {
    private static Logger logger = Logger.getLogger(Pager.class);
    public static final String NEXT = "__NEXT__" ;
    public static final String PREVIOUS = "__PREVIOUS" ;
    public static final String FIRSTPAGE = "__FIRSTPAGE" ;
    public static final String LASTPAGE = "__LASTPAGE" ;
    /** La variable en el formulario HTML que es tomada por el pager
     * para modificar sus pagina
     */
    public static final String PAGERCOMMAND = "_pagercmd_" ;

    private int pagesize = 10 ;
    private int datasize = 0;
    /** Un puntero a la pagina actual. las pagina se indexan desde
     *  1 ... n
     */
    private int curpage = 0;
    public Pager(ArrayList data) {
        datasize = data.size();
        curpage = 1 ;
    }
    public Pager(List<Object> data) {
        datasize = data.size();
        curpage = 1 ;
    }
    public Pager(int si) {
    	datasize = si ;
        curpage = 1 ;
    }
    public int size() {
	if ( datasize == 0 ) {
		return 0  ;
	}
        int resto =  datasize % pagesize  ;
	if ( resto == 0 )  {
		return pagesize ; 
	}
	if ( curpage == numPages()) {
		return resto ;
	}
	return pagesize ;
        /*if ( (curpage != numPages()) || ( datasize==pagesize) ){
            return pagesize ;
        }
	*/
    }
    private int numPages() {
        int np = datasize / pagesize ;
        if (( datasize % pagesize ) != 0 )  {
            np++ ;
        }
        return np ;
    }
    public int sizeData() {
        return datasize ;
    }

    /** indica si deberia haber una referencia a la
     * primera pagina. Solo aparece referencia a la primera
     * cuando estamos en la tercera o m'as paginas
     */
    public boolean hasFirstPage() {
        return ( curpage > 2 ) ;
    }
    /** Indica si deberia haber una referencia a la pagina anterior. La cual
     * solo ocurre si NO estamos en la primera pagina
     */
    public boolean hasPreviousPage()  {
        return (curpage != 1 ) ;
    }
    public boolean hasNextPage() {
        return ( curpage < numPages());
    }
    public boolean hasLastPage() {
        return ( curpage < (numPages()-2));
    }

    /** Obtiene informacion relevannte para construir la orden
     * de paso a la primera pagina.
     */
    public String getFirstPageReference() {
        return "&"+PAGERCOMMAND+"="+FIRSTPAGE ;
    }
    public String getPreviousPageReference() {
        return "&"+PAGERCOMMAND+"="+PREVIOUS ;
    }
    public String getNextPageReference() {
        return "&"+PAGERCOMMAND+"="+NEXT ;
    }
    public String getLastPageReference() {
        return "&"+PAGERCOMMAND+"="+LASTPAGE ;
    }
    public void setPage(String cmd) {
        if ( cmd.equals(NEXT)) {
            curpage++ ;
        }
        if ( cmd.equals(PREVIOUS)) {
            curpage-- ;
        }
        if ( cmd.equals(FIRSTPAGE)) {
            curpage = 1 ;
        }
        if ( cmd.equals(LASTPAGE)) {
            curpage = numPages();
        }
    }
    /** Retorna el indice del primer elemento de la pagina
     * actual
     */
    public int firstIndex() {
        return ( curpage - 1 ) * pagesize + 1 ;
    }
    /** Retorna el indice del ultimo elemento de la pagina
     * actual
     */
    public int lastIndex () {
        if ( curpage == numPages() ) {
            return datasize ;
        }
        return curpage  * pagesize ;
    }
    public String dump() {
        logger.debug("datasize="+datasize+",pagesize="+pagesize+",curpage="+curpage);
        StringBuffer out = new StringBuffer();
        out.append("FIRST=");
        if ( hasFirstPage() ) {
            out.append(getFirstPageReference()) ;
            out.append("|");
        }
        out.append("|PREVIOUS=");
        if ( hasPreviousPage() ) {
            out.append(getPreviousPageReference()) ;
            out.append("|");
        }
        out.append(" " + firstIndex() + " - " + lastIndex() + " de " + datasize + " " ) ;
        out.append("|NEXT=");
        if ( hasNextPage() ) {
            out.append(getNextPageReference()) ;
            out.append("|");
        }
        out.append("|LAST=");
        if ( hasLastPage() ) {
            out.append(getLastPageReference()) ;
            out.append("|");
        }
        return out.toString();
    }

    /** Retorna el indice dentro del arreglo de Datos correspondiente con el indice
     * del paginador.  Ambos se indexan desde 0 .. (n-1)
     */
    public int getAbsoluteIndex(int pagerIndex) throws Exception {
        // El pager inicia en un "0" relativo a la pagina actual y termina
        // en  (pagesize - 1 )
        if ( ( pagerIndex < 0 ) || ( pagerIndex > pagesize ) ) {
            throw new Exception ("Index " + pagerIndex + " out of range, pagesize="+pagesize);
        }
        logger.debug("curpage="+curpage +", pagesize="+ pagesize + ",pagerIndex="+pagerIndex );
        int dataIndex =  (curpage  - 1 ) * pagesize + pagerIndex ;
        logger.debug("returning " + dataIndex );
        return dataIndex ;
    }
    /** Ubica las variables que son propias del PAGER Y aplica los comandos alli indicados
     */
    public void processEvent (HttpServletRequest request) {
        String cmdPager = request.getParameter(PAGERCOMMAND);
        if ( cmdPager == null ) {
            // No hay comando para el pager
            logger.warn("HTML Jnhp no tiene comando pager " + PAGERCOMMAND);
            return ;
        }
        setPage(cmdPager);
    }
	public int getPagesize() {
		return pagesize;
	}
	public void setPagesize(int pagesize) {
		this.pagesize = pagesize;
	}
    

}
