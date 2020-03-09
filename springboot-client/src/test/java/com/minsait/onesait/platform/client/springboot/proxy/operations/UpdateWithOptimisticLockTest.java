/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.client.springboot.proxy.operations;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minsait.onesait.platform.client.RestClient;
import com.minsait.onesait.platform.client.springboot.autoconfigure.ClientIoTBroker;
import com.minsait.onesait.platform.comms.protocol.enums.SSAPQueryType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.OptimisticLockException;
import javax.persistence.Version;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith( SpringRunner.class )
@SpringBootTest
@ComponentScan(basePackages = { "com.minsait"}, lazyInit = false)
public class UpdateWithOptimisticLockTest{

    @Mock
    private ClientIoTBroker ioTBroker;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private Update update;

    @Test
    public void testUpdateWithoutChangingVersion() throws IOException {
        Entity entity =  new Entity("nochange", "1");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(entity);
        JsonNode jsonNode = mapper.readTree(json);
        List<JsonNode> jsonNodes=new ArrayList<>();
        jsonNodes.add(jsonNode);

        when(ioTBroker.init()).thenReturn(this.restClient);

        when(restClient.query("ontology",  String.format(Update.QUERY_BY_OBJECT_ID, "ontology", "nochange"), SSAPQueryType.SQL))
                .thenReturn(jsonNodes);

        when(restClient.updateWithConfirmation("ontology", json, "nochange" )).thenReturn(jsonNode);

        Method method = FakeInterface.class.getDeclaredMethods()[0];
        Object[] args={
          "nochange",
          entity
        };
       update.operation(method, args, ioTBroker, "ontology",  Entity.class, false);
    }

    @Test(expected = OptimisticLockException.class)
    public void testUpdateVersionConflict() throws IOException {
        Entity entity =  new Entity("change", "2");
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(entity);
        JsonNode jsonNode = mapper.readTree(json);
        List<JsonNode> jsonNodes=new ArrayList<>();
        jsonNodes.add(jsonNode);

        when(ioTBroker.init()).thenReturn(this.restClient);

        when(restClient.query("ontology",  String.format(Update.QUERY_BY_OBJECT_ID, "ontology", "change"), SSAPQueryType.SQL))
                .thenReturn(jsonNodes);

        when(restClient.updateWithConfirmation("ontology", json, "change" )).thenReturn(jsonNode);

        Method method = FakeInterface.class.getDeclaredMethods()[0];
        Object[] args={
                "change",
                new Entity("change", "1")
        };
        update.operation(method, args, ioTBroker, "ontology",  Entity.class, false);
    }

}
class Entity{
    private String id;

    @Version
    private String version;

    public Entity(){

    }

    public Entity(String id, String version){
        this.id=id;
        this.version=version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}

interface FakeInterface {
    void updateMethod(String id, Entity entity);
}

