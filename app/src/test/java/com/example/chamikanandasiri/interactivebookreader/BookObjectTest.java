package com.example.chamikanandasiri.interactivebookreader;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;


import static org.junit.Assert.*;

public class BookObjectTest {
    BookObject bookObject;
    final String[] authors ={"a","b"},isbn={"12345678"},imgurl={"Https//:"},cover={"hhh"};
    DownloadContentObject downloadContentObject;
    ArrayList<DownloadContentObject> downloadContentObjects;
    @Before
    public void setUp() throws Exception {
        downloadContentObject=new DownloadContentObject("123456",imgurl,
                "Name","1234",
                "Description","10","fileURL",false,3);
        downloadContentObjects=new ArrayList<>();
        downloadContentObjects.add(downloadContentObject);
        bookObject=new BookObject("1234","Title",authors,isbn,cover,true,downloadContentObjects,"5678","pubname");
    }

    @Test
    public void getBookId() {
        assertEquals("1234",bookObject.getBookId());
    }

    @Test
    public void getTitle() {
        assertEquals("Title",bookObject.getTitle());
    }

    @Test
    public void getAuthors() {
        assertArrayEquals(authors,bookObject.getAuthors());
    }

    @Test
    public void getIsbns() {
        assertArrayEquals(isbn,bookObject.getIsbns());

    }

    @Test
    public void getCovers() {
        assertArrayEquals(cover,bookObject.getCovers());
    }

    @Test
    public void isActive() {
        assertEquals(true,bookObject.isActive());
    }

    @Test
    public void getDownloadContent() {
        assertEquals(downloadContentObjects,bookObject.getDownloadContent());

    }

    @Test
    public void getPublisherId() {
        assertEquals("5678",bookObject.getPublisherId());
    }

    @Test
    public void getPublisherName() {
        assertEquals("pubname",bookObject.getPublisherName());
    }

}