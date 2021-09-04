package org.jpwh.web.util;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * The Servlet spec only unwraps the first level "root" cause in an exception chain.
 *
 * That is of course completely useless, as dozens of layers these days add their
 * exception to the chain while throwing up. So we unwrap the "deep" real root cause
 * from every exception here, then you can handle it with the error-page directives
 * in web.xml.
 */
@WebFilter(urlPatterns = "/*")
public class ExceptionUnwrapFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
       throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (ServletException e) {
            // Unwrap the real root cause and throw away all the intermediate exceptions
            // in the chain... now this real root cause can be handled in web.xml
            throw new ServletException(unwrap(e));
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    public Throwable unwrap(Throwable throwable) throws IllegalArgumentException {
        if (throwable == null) {
            throw new IllegalArgumentException("Cannot unwrap null throwable");
        }
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            throwable = current;
        }
        return throwable;
    }
}
