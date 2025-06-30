package unit

import dao.*
import model.database.*
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import util.Logging

class AnnotationTest {

    private val logger = Logging.logger {}

    @Test
    fun test() {
        check(ComputationDao::class.java, ComputationData::class.java, "Computation", false)
        check(ConclusionDao::class.java, ConclusionData::class.java, "Conclusion", false)
        check(ConditionDao::class.java, ConditionData::class.java, "Condition", false)
        check(DifferentialEquationDao::class.java, DifferentialEquationData::class.java, "DifferentialEquation", true)
        check(
            DifferentialEquationLinkDao::class.java,
            DifferentialEquationLinkData::class.java,
            "DifferentialEquationLink",
            true
        )
        check(
            DifferentialEquationPropertyDao::class.java,
            DifferentialEquationPropertyData::class.java,
            "DifferentialEquationProperty",
            true
        )
        check(InviteDao::class.java, InviteData::class.java, "Invite", false)
        check(LinkDao::class.java, LinkData::class.java, "Link", true)
        check(PropertyDao::class.java, PropertyData::class.java, "Property", true)
        check(ReferenceDao::class.java, ReferenceData::class.java, "Reference", false)
        check(SpaceDao::class.java, SpaceData::class.java, "SpaceData", true)
        check(TheoremDao::class.java, TheoremData::class.java, "Theorem", true)
        check(UserDao::class.java, UserData::class.java, "UserData", false)
    }

    private fun <S, T> check(dao: Class<S>, data: Class<T>, name: String, update: Boolean) {
        checkCreate(dao, data, name)
        if (update) {
            checkUpdate(dao, data, name)
        }
    }

    private fun <S, T> checkCreate(dao: Class<S>, data: Class<T>, name: String) {
        val actualValue = (dao.methods.first { it.name == "create" }.annotations[0] as SqlUpdate).value.trim()
        val actual = actualValue.lines().joinToString(transform = String::trim, separator = " ")
        val members = data.declaredFields.map { it.name }
        val keys = members.joinToString(", ")
        val values = members.joinToString(", ") { ":data.$it" }
        val expected = "INSERT INTO $name ( $keys ) VALUES ( $values )"
        logger.error(expected)
        logger.error(actual)
        Assertions.assertEquals(expected, actual)
    }

    private fun <S, T> checkUpdate(dao: Class<S>, data: Class<T>, name: String) {
        val actualValue = (dao.methods.first { it.name == "update" }.annotations[0] as SqlUpdate).value.trim()
        val actual = actualValue.lines().joinToString(transform = String::trim, separator = " ")
        val members = data.declaredFields.map { it.name }
        val lines = members.joinToString(", ") { "$it = :data.$it" }
        val expected = "UPDATE $name SET $lines WHERE id = :data.id"
        logger.error("expected: $expected")
        logger.error("actual:   $actual")
        Assertions.assertEquals(expected, actual)
    }

}
