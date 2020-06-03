package com.example.chamikanandasiri.interactivebookreader;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.*;

public class WordHandlerTest {
    private  WordHandler wordHandler;
    @Mock
    Context context;
    @Mock
    DataBaseHelper dataBaseHelper;

    @Before
    public void setUp(){
        wordHandler= new WordHandler(dataBaseHelper,context);
    }

    @Test
    public void getWords() {

    }

    @Test
    public void getSimilarWords() {
    }

    @Test
    public void getDefinitionPosByWord() {
    }

    @Test
    public void getWordsByPos() {
    }

    @Test
    public void addWord() {
    }

    @After
    public void tearDown() throws Exception {
    }
}