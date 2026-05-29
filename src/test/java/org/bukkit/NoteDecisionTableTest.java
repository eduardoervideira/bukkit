package org.bukkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoteDecisionTableTest {

    // R1: C1=T (octave out of range) -> E1
    @Test(expected = IllegalArgumentException.class)
    public void r1_octaveOutOfRange() {
        new Note(-1, Note.Tone.A, false);
    }

    // R2: C1=F, C2=F, C6=F, C3..C5 indifferent -> E2
    @Test
    public void r2_octaveInRange_notSharped_anyTone() {
        Note n = new Note(0, Note.Tone.B, false);
        assertEquals(Note.Tone.B, n.getTone());
        assertFalse(n.isSharped());
        assertEquals(0, n.getOctave());
    }

    // R3: octave in {0,1}, plain tone, sharped=false -> E2
    @Test
    public void r3_octaveInRange_plainToneNatural() {
        Note n = new Note(1, Note.Tone.A, false);
        assertEquals(Note.Tone.A, n.getTone());
        assertFalse(n.isSharped());
        assertEquals(1, n.getOctave());
    }

    // R4: octave=2, tone=F, sharped=false -> I2=T, I3=F -> E1
    @Test(expected = IllegalArgumentException.class)
    public void r4_octave2_F_natural() {
        new Note(2, Note.Tone.F, false);
    }

    // R5: octave=2, tone=F, sharped=true -> I2=T, I3=T -> E2 (legal F#2)
    @Test
    public void r5_octave2_F_sharp_legal() {
        Note n = new Note(2, Note.Tone.F, true);
        assertEquals(Note.Tone.F, n.getTone());
        assertTrue(n.isSharped());
        assertEquals(2, n.getOctave());
    }

    // R6: octave=2, tone=B, sharped=true -> rewrite B->C, sharped=false -> I2=F -> E1
    @Test(expected = IllegalArgumentException.class)
    public void r6_octave2_B_sharp_rewritesToC_throws() {
        new Note(2, Note.Tone.B, true);
    }

    // R7 (fixed): octave=2, tone=E, sharped=true -> rewrite E->F, sharped=false -> I3=F -> E1
    @Test(expected = IllegalArgumentException.class)
    public void r7_octave2_E_sharp_rewritesToF_throws() {
        new Note(2, Note.Tone.E, true);
    }

    // R8: octave=2, tone not in {B,E,F}, sharped indifferent -> E1
    @Test(expected = IllegalArgumentException.class)
    public void r8_octave2_otherTone() {
        new Note(2, Note.Tone.A, false);
    }
}
