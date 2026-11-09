package pl.zombek.client.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.zombek.client.core.tracking.PvPSession;
import pl.zombek.client.core.tracking.PvPSessionManager;

import static org.junit.jupiter.api.Assertions.*;

public class PvPSessionTest {
    private PvPSession session;

    @BeforeEach
    void setUp() {
        session = new PvPSession();
    }

    @Test
    void testSessionLifecycleAndAccuracy() {
        assertTrue(session.isActive());
        assertEquals(0, session.getMeleeAttacks());
        assertEquals(0, session.getObservedHits());
        assertEquals(0.0, session.getEstimatedHitRate(), 0.01);

        session.recordAttackAttempt();
        session.recordAttackAttempt();
        session.recordAttackAttempt();
        session.recordAttackAttempt();
        assertEquals(4, session.getMeleeAttacks());

        session.recordObservedHit();
        session.recordObservedHit();
        assertEquals(2, session.getObservedHits());

        // 2 hits out of 4 attacks = 50%
        assertEquals(50.0, session.getEstimatedHitRate(), 0.01);
    }

    @Test
    void testComboAndDamageTracking() {
        session.recordCombo(3);
        session.recordCombo(7);
        session.recordCombo(4);
        assertEquals(7, session.getHighestCombo());

        session.recordDamageReceived(4.5f);
        session.recordDamageReceived(2.0f);
        assertEquals(6.5f, session.getDamageReceived(), 0.01f);
        assertEquals(2, session.getHealthChangeEvents());
    }

    @Test
    void testHealthAndTargetValidation() {
        session.updatePlayerHealth(16.0f, 4.0f);
        assertEquals(16.0f, session.getPlayerHealth(), 0.01f);
        assertEquals(4.0f, session.getPlayerAbsorption(), 0.01f);

        assertFalse(session.hasValidTargetHealth());
        assertEquals(-1.0f, session.getTargetHealth(), 0.01f);

        session.updateTargetHealth(12.5f);
        assertTrue(session.hasValidTargetHealth());
        assertEquals(12.5f, session.getTargetHealth(), 0.01f);

        session.clearTargetHealth();
        assertFalse(session.hasValidTargetHealth());
    }

    @Test
    void testSessionManagerReset() {
        PvPSessionManager manager = PvPSessionManager.getInstance();
        manager.getCurrentSession().recordAttackAttempt();
        manager.getCurrentSession().recordObservedHit();
        manager.getCurrentSession().recordDamageReceived(5.0f);

        manager.resetSession();
        assertEquals(0, manager.getCurrentSession().getMeleeAttacks());
        assertEquals(0, manager.getCurrentSession().getObservedHits());
        assertEquals(0.0f, manager.getCurrentSession().getDamageReceived(), 0.01f);
    }
}
