package br.jus.trf2.xjus;

import java.io.IOException;

import com.crivano.swaggerservlet.ISwaggerApiContext;
import com.crivano.swaggerservlet.SwaggerContext;

public class XjusContext implements ISwaggerApiContext {

	@Override
	public void init(SwaggerContext ctx) {
	}

	@Override
	public void onTryBegin() throws Exception {
	}

	@Override
	public void onTryEnd() throws Exception {
	}

	@Override
	public void onCatch(Exception e) throws Exception {
		throw e;
	}

	@Override
	public void onFinally() throws Exception {
	}

	@Override
	public void close() throws IOException {
	}

}
