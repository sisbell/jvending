/**
 *    Copyright 2003-2010 Shane Isbell
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.jvending.provisioning.dao;

import java.util.List;

import javax.provisioning.BundleDescriptor;

/**
 * Provides services for retrieving bundle descriptors.
 *
 * @author Shane Isbell
 * @since 2.0.0
 */


public interface BundleDescriptorDAO {

    BundleDescriptor getBundleDescriptorFor(String id);

    List<BundleDescriptor> getBundleDescriptors();
    
    List<BundleDescriptor> getBundleDescriptorsByEventId(long eventId);
}
