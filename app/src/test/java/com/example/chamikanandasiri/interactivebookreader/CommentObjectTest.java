package com.example.chamikanandasiri.interactivebookreader;

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class CommentObjectTest {
    final String title="Title",phrase="Phrase",comment="Comment";
    CommentObject commentObject;

    @Before
    public void setup(){
        commentObject=new CommentObject("Title","Phrase","Comment");
    }

    @Test
    public void getTitle() {
        assertEquals(title,commentObject.getTitle());
    }

    @Test
    public void getPhrase() {
        assertEquals(phrase,commentObject.getPhrase());
    }

    @Test
    public void getComment() {
        assertEquals(comment,commentObject.getComment());
    }

}