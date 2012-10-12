/**
 * Copyright 2012 Bob Browning
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.broker.plugin;

import com.google.common.base.Objects;
import com.ibm.broker.plugin.visitor.DefaultMbMessageVisitor;
import com.ibm.broker.trace.Trace;
import com.ibm.broker.trace.TraceRule;
import junit.framework.Assert;
import org.jaxen.XPath;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Bob Browning <bob.browning@pressassociation.com>
 */
@RunWith(PowerMockRunner.class)
@MockPolicy(MbMockPolicy.class)
public class MbJUnitRunnerTest {

    private static final Logger logger = LoggerFactory.getLogger(MbJUnitRunnerTest.class);

    private DefaultMbMessageVisitor visitor = new DefaultMbMessageVisitor() {
        @Override public void visit(PseudoNativeMbMessageAssembly assembly) {
            System.out.println(assembly.toString());
        }

        @Override
        public void visit(PseudoNativeMbMessage message) {
            System.out.println(message.toString());
        }

        @Override
        public void visit(PseudoNativeMbElement element) {
            System.out.println(element.toString());
        }
    };

    @Rule
    public TraceRule traceRule = new TraceRule();

    @Test
    public void testTracer() throws Exception {
        Trace.setLogLevel(Trace.DEBUGTRACE);
        Trace.logNamedEntry("name", "1");
        Trace.logNamedDebugEntry("debugName", "1");
        Trace.logNamedDebugTrace("traceName", "1", "2");
        Trace.logNamedUserTrace("userTraceName", "1", "2", 0L, "5");
        Trace.logNamedUserDebugTrace("userDebugName", "1", "2", 0L, "5");
    }

    @Test
    public void testEquals() throws Exception {
        MbMessage m = new MbMessage();
        MbElement xmlnsc = m.getRootElement().createElementAsFirstChild(MbXMLNSC.PARSER_NAME);
        MbElement xmlnsc2 = m.getRootElement().getLastChild();
        logger.debug(Objects.toStringHelper(xmlnsc).add("hashCode", xmlnsc.hashCode()).add("equals", xmlnsc2.equals(xmlnsc)).toString());
    }

    @Test
    public void testCreationOfMbMessage() throws Exception {
        MbMessage message = new MbMessage();
        MbElement e = message.getRootElement();
        Assert.assertNotNull(e);
    }

    private class MbTestObject {
        final MbMessage message;
        final MbElement child;
        final MbElement root;

        private MbTestObject(MbMessage message, MbElement child) throws MbException {
            this.message = message;
            this.root = message.getRootElement();
            this.child = child;
        }
    }

    private MbTestObject createMessageAndFirstChild() throws MbException {
        final MbMessage message = new MbMessage();
        MbElement e = message.getRootElement();
        Assert.assertNotNull(e);

        final MbElement firstChild = e.createElementAsFirstChild(MbXMLNSC.PARSER_NAME);
//        Assert.assertTrue(firstChild.equals(e.getFirstChild()));
        MbAssert.assertEquals(firstChild, e.getFirstChild());
        MbAssert.assertEquals(firstChild, e.getLastChild());
        MbAssert.assertEquals(e.getFirstChild(), e.getLastChild());
        Assert.assertNull(firstChild.getPreviousSibling());
        Assert.assertNull(firstChild.getNextSibling());
        MbAssert.assertEquals(e, firstChild.getParent());
        return new MbTestObject(message, firstChild);
    }

    @Test
    public void testCreationOfFirstChild() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement e = initialState.root;
        MbElement firstChild = initialState.child;

        MbElement nextSibling = e.createElementAsFirstChild(MbXMLNSC.PARSER_NAME);
        MbAssert.assertEquals(nextSibling, e.getFirstChild());
        MbAssert.assertEquals(firstChild, e.getLastChild());
        Assert.assertNull(nextSibling.getPreviousSibling());
        MbAssert.assertEquals(firstChild, nextSibling.getNextSibling());
        MbAssert.assertEquals(nextSibling, firstChild.getPreviousSibling());
        MbAssert.assertEquals(e, nextSibling.getParent());
    }

    @Test
    public void testCreationOfLastChild() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement e = initialState.root;
        MbElement firstChild = initialState.child;

        MbElement nextSibling = e.createElementAsLastChild(MbXMLNSC.PARSER_NAME);
        MbAssert.assertEquals(firstChild, e.getFirstChild());
        MbAssert.assertEquals(nextSibling, e.getLastChild());
        Assert.assertNull(nextSibling.getNextSibling());
        MbAssert.assertEquals(nextSibling, firstChild.getNextSibling());
        MbAssert.assertEquals(firstChild, nextSibling.getPreviousSibling());
        MbAssert.assertEquals(e, nextSibling.getParent());
    }


    @Test
    public void testCreationOfPreviousSibling() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement e = initialState.root;
        MbElement firstChild = initialState.child;

        MbElement nextSibling = firstChild.createElementBefore(MbXMLNSC.PARSER_NAME);
        MbAssert.assertEquals(nextSibling, e.getFirstChild());
        MbAssert.assertEquals(firstChild, e.getLastChild());
        Assert.assertNull(nextSibling.getPreviousSibling());
        MbAssert.assertEquals(firstChild, nextSibling.getNextSibling());
        MbAssert.assertEquals(nextSibling, firstChild.getPreviousSibling());
        MbAssert.assertEquals(e, nextSibling.getParent());
    }

    @Test
    public void testCreationOfNextSibling() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement e = initialState.root;
        MbElement firstChild = initialState.child;

        MbElement nextSibling = firstChild.createElementAfter(MbXMLNSC.PARSER_NAME);
        MbAssert.assertEquals(firstChild, e.getFirstChild());
        MbAssert.assertEquals(nextSibling, e.getLastChild());
        Assert.assertNull(nextSibling.getNextSibling());
        MbAssert.assertEquals(nextSibling, firstChild.getNextSibling());
        MbAssert.assertEquals(firstChild, nextSibling.getPreviousSibling());
        MbAssert.assertEquals(e, nextSibling.getParent());
    }

    @Test
    public void testSetValue() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement firstChild = initialState.child;
        MbElement firstField = firstChild.createElementAsFirstChild(MbXMLNSC.FIELD);
        firstField.setValue(1);
        Assert.assertEquals(1, firstField.getValue());
        Assert.assertEquals("1", firstField.getValueAsString());
    }

    @Test
    public void testInteger() throws Exception {
        testCreateWithValue(Integer.class, 1);
    }

    @Test
    public void testLong() throws Exception {
        testCreateWithValue(Long.class, 1L);
    }

    @Test
    public void testDouble() throws Exception {
        testCreateWithValue(Double.class, 1.0d);
    }

    @Test
    public void testBigDecimal() throws Exception {
        testCreateWithValue(BigDecimal.class, new BigDecimal("10000000000000000000.01"));
    }

    @Test
    public void testBoolean() throws Exception {
        testCreateWithValue(Boolean.class, Boolean.TRUE);
    }

    @Test
    public void testString() throws Exception {
        testCreateWithValue(String.class, "XYZZY");
    }

    @Test
    public void testBitSet() throws Exception {
        BitSet b = new BitSet();
        b.set(8, true);
        testCreateWithValue(BitSet.class, b);
    }

    public <T> void createNamedValue(MbElement parent, Class<T> klass, String name, T value) throws Exception {
        MbElement firstField = parent.createElementAsFirstChild(MbXMLNSC.FIELD, name, value);
        Assert.assertEquals(name, firstField.getName());
        Assert.assertEquals(klass, firstField.getValue().getClass());
        Assert.assertEquals(value, firstField.getValue());
        Assert.assertEquals(String.valueOf(value), firstField.getValueAsString());
    }

    public <T> void testCreateWithValue(Class<T> klass, T value) throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        createNamedValue(initialState.child, klass, "field", value);
    }

    @Test
    public void testCreateWithMbTimestampValue() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement firstChild = initialState.child;
        MbTimestamp timestamp = new MbTimestamp(2012, 1, 1, 1, 1, 1);
        timestamp.setTimeZone(TimeZone.getTimeZone("GMT"));
        MbElement firstField = firstChild.createElementAsFirstChild(MbXMLNSC.FIELD, "fieldName", timestamp);
        logger.debug(firstField.toString());
        Assert.assertEquals(MbTimestamp.class, firstField.getValue().getClass());
        Assert.assertEquals(timestamp.getTimeInMillis(), ((MbTimestamp) firstField.getValue()).getTimeInMillis());
//        MbAssert.assertEquals(timestamp.toString(), firstField.getValueAsString());
    }

    @Test
    public void testCreateWithMbTimeValue() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement firstChild = initialState.child;
        MbTime time = new MbTime(1, 1, 1);
        time.setTimeZone(TimeZone.getTimeZone("GMT"));
        MbElement firstField = firstChild.createElementAsFirstChild(MbXMLNSC.FIELD, "fieldName", time);
        logger.debug(firstField.toString());
        Assert.assertEquals(MbTime.class, firstField.getValue().getClass());
        Assert.assertEquals(time.getTimeInMillis(), ((MbTime) firstField.getValue()).getTimeInMillis());
//        MbAssert.assertEquals(timestamp.toString(), firstField.getValueAsString());
    }

    @Test
    public void testCreateWithMbDateValue() throws Exception {
        MbTestObject initialState = createMessageAndFirstChild();
        MbElement firstChild = initialState.child;
        MbDate date = new MbDate(2012, 1, 1);
        MbElement firstField = firstChild.createElementAsFirstChild(MbXMLNSC.FIELD, "fieldName", date);
        Assert.assertEquals(MbDate.class, firstField.getValue().getClass());
        Assert.assertEquals(date.getTimeInMillis(), ((MbDate) firstField.getValue()).getTimeInMillis());
//        MbAssert.assertEquals(timestamp.toString(), firstField.getValueAsString());
    }

    private MbTestObject createTestTree() throws Exception {
        MbTestObject o = createMessageAndFirstChild();
        for (int i = 0; i < 10; i++) {
            createNamedValue(o.child, Integer.class, "field-" + i, i % 2);
        }
        MbElement folder = o.child.createElementAsLastChild(MbXMLNSC.FOLDER);
        for (int i = 0; i < 10; i++) {
            createNamedValue(folder, Integer.class, "field-" + i, i % 2);
        }
        return o;
    }

    @Test
    public void testJaxenXPath() throws Exception {
        MbTestObject o = createTestTree();
        PseudoMbMessageNavigator nav = new PseudoMbMessageNavigator();
        XPath xpath = nav.parseXPath("//field-1");
        List list = xpath.selectNodes(o.message);
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testMbXPath() throws Exception {
        MbTestObject o = createTestTree();
        MbXPath x = new MbXPath("/field-1");
        List result = (List) o.root.evaluateXPath(x);
        Assert.assertEquals(1, result.size());

        x = new MbXPath("/field-1", o.root.getLastChild());
        result = (List) o.root.evaluateXPath(x);
        Assert.assertEquals(1, result.size());

        x = new MbXPath("/field-1", o.root);
        result = (List) o.root.evaluateXPath(x);
        Assert.assertEquals(0, result.size());

        MbXPath x2 = new MbXPath("//*", o.root);
        result = (List) o.root.evaluateXPath(x2);
        Assert.assertEquals(22, result.size());

        result = (List) o.root.evaluateXPath("//*");
        Assert.assertEquals(22, result.size());
    }

    @Test
    public void testGetFirstElementByPath() throws Exception {
        MbTestObject o = createTestTree();
        MbElement e = o.root.getFirstElementByPath("/field-1");
        Assert.assertNull(e);
        e = o.root.getFirstElementByPath("/XMLNSC/field-1");
        Assert.assertNotNull(e);
    }

    @Test
    public void testGetAllElementByPath() throws Exception {
        MbTestObject o = createTestTree();
        MbElement[] elements = o.root.getAllElementsByPath("//field-1");
        Assert.assertNotNull(elements);
        Assert.assertEquals(2, elements.length);
    }

    @Test
    public void testGetAllElement() throws Exception {
        MbTestObject o = createTestTree();
        List<?> elements = (List) o.root.evaluateXPath("//field-1");
        Assert.assertNotNull(elements);
        Assert.assertEquals(2, elements.size());
        Assert.assertTrue(elements.get(0) instanceof MbElement);
    }

    @Test
    public void testMessageAssembly() throws Exception {
        MbMessageAssembly assembly = PseudoNativeMbMessageAssemblyManager.getInstance().createBlankReadOnlyMessageAssembly();
        Assert.assertNotNull(assembly);
        Assert.assertNotNull(assembly.getMessage());
        Assert.assertFalse(assembly.getLocalEnvironment().isReadOnly());
        Assert.assertFalse(assembly.getGlobalEnvironment().isReadOnly());
        Assert.assertFalse(assembly.getExceptionList().isReadOnly());
        Assert.assertTrue(assembly.getMessage().isReadOnly());
    }

    @Test
    public void testOutMessageAssembly() throws Exception {
        MbMessageAssembly assembly = PseudoNativeMbMessageAssemblyManager.getInstance().createBlankReadOnlyMessageAssembly();
        Assert.assertTrue(assembly.getMessage().isReadOnly());
        MbMessageAssembly outAssembly = new MbMessageAssembly(assembly, new MbMessage(assembly.getMessage()));
        Assert.assertFalse(outAssembly.getMessage().isReadOnly());
        PseudoNativeMbMessageAssemblyManager.getInstance().accept(visitor);
    }

    @Test
    public void testMbElementClone() throws Exception {
        MbMessage message = new MbMessage();
        MbElement root = message.getRootElement();
        Assert.assertNotNull(root);

        root.setValue(new MbDate(2000, 1, 1));

        MbElement clone = root.copy();
        Assert.assertNotNull(clone);

        Assert.assertNotSame(root, clone);
        MbDate date = (MbDate) clone.getValue();
        date.set(2012, 12, 31);
        Assert.assertNotSame(root.getValue(), date);
    }

    @Test
    public void testCopy() throws Exception {
        MbMessageAssembly assembly = PseudoNativeMbMessageAssemblyManager.getInstance().createBlankReadOnlyMessageAssembly();
        MbMessage message = new MbMessage(assembly.getMessage());
        Assert.assertNotNull(message);
        Assert.assertNotSame(assembly.getMessage().getRootElement(), message.getRootElement());
    }

    @Test(expected = MbException.class)
    public void testMbException() throws Exception {
        throw new MbException("a", "b", "c", "d", "", new Object[]{""});
    }

    @After
    public void dumpMessages() throws Exception {
        logger.info(" *** START *** ");
        try {
            if(!PseudoNativeMbMessageAssemblyManager.getInstance().getAllocations().isEmpty()) {
                PseudoNativeMbMessageAssemblyManager.getInstance().accept(visitor);
            } else {
                PseudoNativeMbMessageManager.getInstance().accept(visitor);
            }
        } finally {
            logger.info(" *** END *** ");
        }
    }

}
