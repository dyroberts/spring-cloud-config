/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.config.server.environment;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.support.AbstractScmAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author Dave Syer
 *
 */
public abstract class AbstractScmEnvironmentRepository extends AbstractScmAccessor
		implements EnvironmentRepository, SearchPathLocator, Ordered {

	private EnvironmentCleaner cleaner = new EnvironmentCleaner();
	private int order = Ordered.LOWEST_PRECEDENCE;

	public AbstractScmEnvironmentRepository(ConfigurableEnvironment environment) {
		super(environment);
	}

	public AbstractScmEnvironmentRepository(ConfigurableEnvironment environment, AbstractScmEnvironmentProperties properties) {
		super(environment, properties);
		if (properties != null) {
			this.order = properties.getOrder() == null ? this.order : properties.getOrder();
		}
	}

	@Override
	public synchronized Environment findOne(String application, String profile, String label) {
		NativeEnvironmentRepository delegate = new NativeEnvironmentRepository(
				getEnvironment());
		Locations locations = getLocations(application, profile, label);
		delegate.setSearchLocations(locations.getLocations());
		Environment result = delegate.findOne(application, profile, "");
		result.setVersion(locations.getVersion());
		result.setLabel(label);
		return this.cleaner.clean(result, getWorkingDirectory().toURI().toString(),
				getUri());
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
