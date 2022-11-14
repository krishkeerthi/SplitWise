package com.example.splitwise.util

import junit.framework.Assert.assertEquals
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

internal class HelperFunctionsLocalTest {

   @Test
    fun correctPhoneNumberCheck() {
       val correctNumber = 9483929332
       //assertEquals(true, numberCheck(correctNumber))
       assertThat(numberCheck(correctNumber), `is`(true))
    }

    @Test
    fun IncorrectPhoneNumberCheck(){
        val wrongNumber = 123456789L
        //assertEquals(false, numberCheck(wrongNumber))
        assertThat(numberCheck(wrongNumber), `is`(false))
    }


}