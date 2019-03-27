/**
 *  This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License. 
 *  To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/ or send a letter to Creative 
 *  Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package fr.philae.femto;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

// Mockito

/**
 * Tests pour la Caisse en utilisant un mock pour la classe Scanette.
 *
 * IMPORTANT : utiliser @RunWith(MokitoJUnitRunner.Silent.class) pour
 *  éviter les erreurs liées au "unnecessary" stubbings.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TestCaisse {

    private MaCaisse maCaisse;
    private Scanette maScanette;

    @Before
    public void setUp() throws ProductDBFailureException {
        maCaisse = spy(new MaCaisse("./target/classes/csv/produitsOK.csv"));
        maScanette = Mockito.mock(Scanette.class);
    }

    @Test
    public void testAFairePasser() {
        // pour vous éviter de le tester...
        assertEquals(-1, maCaisse.connexion(null));

        // ...et pour vous obliger à rajouter la méthode "relectureEffectuee"
        // à la classe Scanette
        when(maScanette.relectureEffectuee()).thenReturn(false);

        // ...et vous illustrer le fonctionnement de l'espion
        doReturn(false).when(maCaisse).demandeRelecture();
        // au lieu de when(maCaisse.demandeRelecture()).thenReturn(false);
        for (int i=0; i < 1000; i++) {
            assertFalse(maCaisse.demandeRelecture());
        }
    }

}