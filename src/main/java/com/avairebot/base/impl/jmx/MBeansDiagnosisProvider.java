/*
 * MBeansDiagnosisProvider.java
 * 
 * Copyright (c) 2009, Ralf Biedert All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the author nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.avairebot.base.impl.jmx;

import java.lang.management.ManagementFactory;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * @author rb
 *
 */
public class MBeansDiagnosisProvider implements DynamicMBean {
    /**
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws NullPointerException
     */
    public MBeansDiagnosisProvider() throws InstanceAlreadyExistsException,
                                    MBeanRegistrationException,
                                    NotCompliantMBeanException,
                                    MalformedObjectNameException, ReflectionException,
                                    MBeanException, NullPointerException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        System.out.println(server.getMBeanCount());
        for (Object object : server.queryMBeans(new ObjectName("*:*"), null))
            System.out.println(((ObjectInstance) object).getObjectName());

        ObjectName name = new ObjectName("com.javatutor.insel.jmx:type=MBeansDiagnosisProvider");
        server.registerMBean(this, name);
    }

    /**
     * @param args
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     * @throws MalformedObjectNameException
     * @throws ReflectionException
     * @throws MBeanException
     * @throws NullPointerException
     * @throws InterruptedException
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) throws InstanceAlreadyExistsException,
                                          MBeanRegistrationException,
                                          NotCompliantMBeanException,
                                          MalformedObjectNameException,
                                          ReflectionException, MBeanException,
                                          NullPointerException, InterruptedException {
        new MBeansDiagnosisProvider();
        Thread.sleep(1000 * 1000);
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException,
                                                MBeanException, ReflectionException {
        // TODO Auto-generated method stub
        System.out.println("getAttr " + attribute);

        return null;
    }

    public AttributeList getAttributes(String[] attributes) {
        System.out.println("getAttrs");

        // TODO Auto-generated method stub
        return null;
    }

    public MBeanInfo getMBeanInfo() {
        System.out.println("getM");
        MBeanAttributeInfo mai = null;
        try {
            mai = new MBeanAttributeInfo("mname", "Desccasas", MBeansDiagnosisProvider.class.getMethod("test"), null);
        } catch (IntrospectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MBeanNotificationInfo mni = new MBeanNotificationInfo(new String[] {"asd"}, "nmnm!", "asdklajsdasl");
        return new MBeanInfo("clzName", "Desc", new MBeanAttributeInfo[] { mai }, new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[] {mni});

    }

    public Object invoke(String actionName, Object[] params, String[] signature)
                                                                                throws MBeanException,
                                                                                ReflectionException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
                                                 InvalidAttributeValueException,
                                                 MBeanException, ReflectionException {
        // TODO Auto-generated method stub
        System.out.println("setAttr");

    }

    public AttributeList setAttributes(AttributeList attributes) {
        System.out.println("setAttrrs");

        return null;
    }

    /**
     * @return .
     */
    public int test() {

        System.out.println("Test");
        return 123;
    }
}
