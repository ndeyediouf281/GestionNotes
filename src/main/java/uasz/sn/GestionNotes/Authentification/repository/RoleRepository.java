package uasz.sn.GestionNotes.Authentification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uasz.sn.GestionNotes.Authentification.model.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {
    @Query("SELECT r FROM Role r WHERE r.role = :role")
    Optional<Role> findRoleByRole(@Param("role") String role);

}
