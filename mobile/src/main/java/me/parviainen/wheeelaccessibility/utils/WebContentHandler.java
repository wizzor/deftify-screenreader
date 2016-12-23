/*
 * Copyright (C) 2011 Google Inc.
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

package me.parviainen.wheeelaccessibility.utils;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;
import java.util.Stack;

/**
 * A handler for parsing simple HTML from Android WebView.
 */
public class WebContentHandler extends DefaultHandler {
    /** Maps input type attribute to element description. */
    private final Map<String, String> mInputTypeToDesc;

    /** Maps ARIA role attribute to element description. */
    private final Map<String, String> mAriaRoleToDesc;

    /** Map tags to element description. */
    private final Map<String, String> mTagToDesc;

    /** A stack for storing post-order text generated by opening tags. */
    private Stack<String> mPostorderTextStack;

    /** Builder for a string to be spoken based on parsed HTML. */
    private StringBuilder mOutputBuilder;

    /**
     * Initializes the handler with maps that provide descriptions for relevant
     * features in HTML.
     *
     * @param htmlInputMap A mapping from input types to text descriptions.
     * @param htmlRoleMap A mapping from ARIA roles to text descriptions.
     * @param htmlTagMap A mapping from common tags to text descriptions.
     */
    public WebContentHandler(Map<String, String> htmlInputMap, Map<String, String> htmlRoleMap,
            Map<String, String> htmlTagMap) {
        mInputTypeToDesc = htmlInputMap;
        mAriaRoleToDesc = htmlRoleMap;
        mTagToDesc = htmlTagMap;
    }

    @Override
    public void startDocument() {
        mOutputBuilder = new StringBuilder();
        mPostorderTextStack = new Stack<>();
    }

    /**
     * Depending on the type of element, generate text describing its conceptual
     * value and role and add it to the output. The role text is spoken after
     * any content, so it is added to the stack to wait for the closing tag.
     */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) {
        fixWhiteSpace();
        final String ariaLabel = attributes.getValue("aria-label");
        final String alt = attributes.getValue("alt");
        final String title = attributes.getValue("title");

        if (ariaLabel != null) {
            mOutputBuilder.append(ariaLabel);
        } else if (alt != null) {
            mOutputBuilder.append(alt);
        } else if (title != null) {
            mOutputBuilder.append(title);
        }

        /*
         * Add role text to the stack so it appears after the content. If there
         * is no text we still need to push a blank string, since this will pop
         * when this element ends.
         */
        final String role = attributes.getValue("role");
        final String roleName = mAriaRoleToDesc.get(role);
        final String type = attributes.getValue("type");
        final String tagInfo = mTagToDesc.get(name.toLowerCase());

        if (roleName != null) {
            mPostorderTextStack.push(roleName);
        } else if (name.equalsIgnoreCase("input") && (type != null)) {
            final String typeInfo = mInputTypeToDesc.get(type.toLowerCase());

            if (typeInfo != null) {
                mPostorderTextStack.push(typeInfo);
            } else {
                mPostorderTextStack.push("");
            }
        } else if (tagInfo != null) {
            mPostorderTextStack.push(tagInfo);
        } else {
            mPostorderTextStack.push("");
        }

        /*
         * The value should be spoken as long as the element is not a form
         * element with a non-human-readable value.
         */
        final String value = attributes.getValue("value");

        if (value != null) {
            String elementType = name;

            if (name.equalsIgnoreCase("input") && (type != null)) {
                elementType = type;
            }

            if (!elementType.equalsIgnoreCase("checkbox") && !elementType.equalsIgnoreCase("radio")) {
                fixWhiteSpace();
                mOutputBuilder.append(value);
            }
        }
    }

    /**
     * Character data is passed directly to output.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        mOutputBuilder.append(ch, start, length);
    }

    /**
     * After the end of an element, get the post-order text from the stack and
     * add it to the output.
     */
    @Override
    public void endElement(String uri, String localName, String name) {
        final String postorderText = mPostorderTextStack.pop();

        if (postorderText.length() > 0) {
            fixWhiteSpace();
        }

        mOutputBuilder.append(postorderText);
    }

    /**
     * Ensure the output string has a character of whitespace before adding
     * another word.
     */
    void fixWhiteSpace() {
        final int index = mOutputBuilder.length() - 1;

        if (index >= 0) {
            final char lastCharacter = mOutputBuilder.charAt(index);

            if (!Character.isWhitespace(lastCharacter)) {
                mOutputBuilder.append(" ");
            }
        }
    }

    /**
     * Get the processed string in mBuilder. Call this after parsing is done to
     * get the finished output.
     *
     * @return A string with HTML tags converted to descriptions suitable for
     *         speaking.
     */
    public String getOutput() {
        return mOutputBuilder.toString();
    }
}
