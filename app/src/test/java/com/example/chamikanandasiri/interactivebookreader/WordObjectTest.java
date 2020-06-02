package com.example.chamikanandasiri.interactivebookreader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WordObjectTest {
    WordObject wordObject;

    @Before
    public void setUp() throws Exception {
        wordObject=new WordObject("Hello","Definition","2",2);
    }

    @Test
    public void getWord() {
        assertEquals("Hello",wordObject.getWord());
    }
    @Test
    public void getDefinition() {
        assertEquals("Definition",wordObject.getDefinition());

    }

    @Test
    public void getPartOfSpeech() {
        assertEquals("2",wordObject.getPartOfSpeech());

    }

}