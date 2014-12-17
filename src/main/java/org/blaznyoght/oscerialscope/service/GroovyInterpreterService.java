package org.blaznyoght.oscerialscope.service;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.util.Log;

public class GroovyInterpreterService {
	private static final Logger LOG = LogManager.getLogger(GroovyInterpreterService.class);
	
	List<PrintWriter> ouputs = new ArrayList<>();
	
	private int bufferSize = 1;
	
	public ByteArrayOutputStream process(String groovyScript, byte[] inBuf) throws IOException, ResourceException, ScriptException {
		String wrapperGroovy = IOUtils.toString(getClass().getResourceAsStream("/wrapper.groovy"));
		
		ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
		Binding binding = new Binding();
		binding.setVariable("inBuf", inBuf);
		binding.setVariable("outBuf", outBuf);
		binding.setVariable("groovyScript", groovyScript);
		binding.setVariable("bufferSize", bufferSize);
		GroovyShell shell = new GroovyShell(binding);
		String script = wrapperGroovy.replaceAll("%script%", groovyScript);
		LOG.info(script);
		shell.evaluate(script);
//		return (ByteArrayOutputStream) binding.getVariable("outBuf");
		return outBuf;
	}
}
