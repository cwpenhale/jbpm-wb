/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workbench.forms.display.backend.provider.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.workbench.forms.service.providing.ProcessRenderingSettings;
import org.jbpm.workbench.forms.service.providing.model.ProcessDefinition;
import org.jbpm.workbench.forms.display.api.KieWorkbenchFormRenderingSettings;
import org.jbpm.workbench.forms.display.backend.provider.AbstractFormProvidingEngineTest;
import org.jbpm.workbench.forms.display.backend.provider.AbstractKieWorkbenchFormsProvider;
import org.jbpm.workbench.forms.display.backend.provider.ProcessFormsValuesProcessor;
import org.jbpm.workbench.forms.display.backend.provider.model.Invoice;
import org.jbpm.workbench.forms.display.backend.provider.model.InvoiceLine;
import org.junit.Test;
import org.kie.workbench.common.forms.dynamic.service.context.generation.dynamic.BackendFormRenderingContextManager;
import org.kie.workbench.common.forms.jbpm.service.bpmn.DynamicBPMNFormGenerator;
import org.kie.workbench.common.forms.serialization.FormDefinitionSerializer;
import org.mockito.Mock;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public abstract class AbstractStartProcessFormTest<PROVIDER extends AbstractKieWorkbenchFormsProvider> extends AbstractFormProvidingEngineTest<ProcessRenderingSettings, ProcessFormsValuesProcessor, PROVIDER> {

    @Mock
    protected ProcessDefinition process;

    @Test
    public void testFormProvider() {
        KieWorkbenchFormRenderingSettings result = workbenchFormsProvider.render( generateSettings() );

        checkRenderingSettings( result );
    }

    @Override
    protected ProcessRenderingSettings generateSettings() {

        when( process.getId() ).thenReturn( "invoices" );

        Map<String, String> formData = new HashMap<>();

        formData.put( "invoice", Invoice.class.getName() );

        return new ProcessRenderingSettings( process,
                                             formData,
                                             getFormContent(),
                                             marshallerContext );
    }

    @Override
    protected ProcessFormsValuesProcessor getProcessorInstance( FormDefinitionSerializer formSerializer,
                                                                BackendFormRenderingContextManager contextManager,
                                                                DynamicBPMNFormGenerator dynamicBPMNFormGenerator ) {
        return new ProcessFormsValuesProcessor( formSerializer, contextManager, dynamicBPMNFormGenerator );
    }

    @Override
    protected Map<String, Object> getFormValues() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> client = new HashMap<>();
        client.put( "id", new Long( 1234 ) );
        client.put( "name", "John Snow" );
        client.put( "address", "Winterfell" );

        List<Map<String, Object>> lines = new ArrayList<>();

        Map<String, Object> line = new HashMap<>();

        line.put( "product", "Really Dangerous Sword" );
        line.put( "quantity", 1 );
        line.put( "price", 100.5 );
        line.put( "total", 100.5 );

        lines.add( line );

        Map<String, Object> invoice = new HashMap<>();
        invoice.put( "client", client );
        invoice.put( "lines", lines );
        invoice.put( "total", 100.5 );
        invoice.put( "comments", "Everything was perfect" );
        invoice.put( "date", new Date() );

        result.put( "invoice", invoice );

        return result;
    }

    @Override
    protected void checkRuntimeValues( Map<String, Object> result ) {
        assertNotNull( "There should be an invoice on the result Map", result.get( "invoice" ) );

        assertTrue( "There should be an invoice on the result Map", result.get( "invoice" ) instanceof Invoice );

        Invoice invoice = (Invoice) result.get( "invoice" );

        assertNotNull( "Invoice should have a client", invoice.getClient() );

        assertEquals( invoice.getClient().getId(), new Long( 1234 ) );
        assertEquals( "John Snow", invoice.getClient().getName() );
        assertEquals( "Winterfell", invoice.getClient().getAddress() );

        assertNotNull( invoice.getDate() );
        assertNotNull( invoice.getComments() );
        assertEquals( invoice.getTotal(), new Double( 100.5 ) );

        assertNotNull( invoice.getLines() );
        assertTrue( invoice.getLines().size() == 1 );

        InvoiceLine line = invoice.getLines().get( 0 );

        assertEquals( "Really Dangerous Sword", line.getProduct() );
        assertEquals( new Integer( 1 ), line.getQuantity() );
        assertEquals( new Double( 100.5 ), line.getPrice() );
        assertEquals( new Double( 100.5 ), line.getTotal() );

    }
}
