package com.tdd.expensetracker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class AppTest 
{
   
    @Test
    public void shouldAnswerWithTrue()
    {
    	App app = new App();
    	app.main(null);
        assertTrue( true );
    }
}
