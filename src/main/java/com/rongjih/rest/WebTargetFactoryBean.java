package com.rongjih.rest;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

/**
 * @author rongjihuang@gmail.com
 */
public class WebTargetFactoryBean implements FactoryBean<WebTarget>, InitializingBean, DisposableBean, ApplicationContextAware {
	private final static Logger logger = LoggerFactory.getLogger(WebTargetFactoryBean.class);
	private String[] componentPackages;     // auto register packages
	private Class<?>[] componentClasses;    // register components

	private ApplicationContext context;
	private JerseyTest jerseyTest;
	private WebTarget target;

	static {
		// bridge java.util.logging to slf4j
		java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager().getLogger("");
		java.util.logging.Handler[] handlers = rootLogger.getHandlers();
		for (java.util.logging.Handler handler : handlers) rootLogger.removeHandler(handler);
		org.slf4j.bridge.SLF4JBridgeHandler.install();
	}

	@Override
	public WebTarget getObject() throws Exception {
		return target;
	}

	@Override
	public Class<?> getObjectType() {
		return WebTarget.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// idea from http://stackoverflow.com/questions/24509754/force-jersey-to-read-mocks-from-jerseytest
		jerseyTest = new JerseyTest() {
			@Override
			protected Application configure() {
				ResourceConfig rc = new ResourceConfig();

				// inject spring context, avoid JerseyTest create a new one.
				// see org.glassfish.jersey.server.spring.SpringComponentProvider.createSpringContext()
				rc.property("contextConfig", WebTargetFactoryBean.this.context);

				// other init
				WebTargetFactoryBean.this.configure(rc);
				return rc;
			}
		};

		logger.info("setUp jerseyTest instance");
		jerseyTest.setUp();

		// init WebTarget instance
		target = jerseyTest.target();
	}

	@Override
	public void destroy() throws Exception {
		logger.info("destroy jerseyTest instance");
		jerseyTest.tearDown();
	}

	protected void configure(ResourceConfig rc) {
		// register components
		Class<?>[] componentClasses = getComponentClasses();
		if (componentClasses != null) for (Class<?> resource : componentClasses) rc.register(resource);

		// auto register package's components
		String[] packages = getComponentPackages();
		if (packages != null) for (String p : packages) rc.packages(p);

		// only log out header, URL
		//rc.register(LoggingFilter.class);// header,URL

		// log out header, URL, body
		rc.register(new LoggingFilter(java.util.logging.Logger.getLogger(LoggingFilter.class.getName()), true));
	}

	public String[] getComponentPackages() {
		return componentPackages;
	}

	public void setComponentPackages(String[] componentPackages) {
		this.componentPackages = componentPackages;
	}

	public Class<?>[] getComponentClasses() {
		return componentClasses;
	}

	public void setComponentClasses(Class<?>[] componentClasses) {
		this.componentClasses = componentClasses;
	}
}