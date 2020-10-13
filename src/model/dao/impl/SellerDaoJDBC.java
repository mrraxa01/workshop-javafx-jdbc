package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class SellerDaoJDBC implements SellerDao {

	private Connection conn;
	
	public SellerDaoJDBC(Connection conn) {
		this.conn = conn;
	}
	@Override
	public void insert(Seller obj) {
		
		PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement(
					"INSERT INTO seller "
					+"(Name, Email, BirthDate, BaseSalary, DepartmentId) "
					+"VALUES "
					+"(?, ?, ?, ?, ?) ",
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			
			int rowsAffected = st.executeUpdate();
			//para verificar se a inserção obteve sucesso, vamos verrificar
			//se foi criado um id para objeto e já devolver o obj com o id 
			//gerado pelo banco de dados
			
			if(rowsAffected >0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1); // a chave gerada vem na coluna 1
					obj.setId(id);
					//obj.setId(rs.getInt(1)); poderia ser assim direto
					DB.closeResultSet(rs);
				}
			}else {
				throw new DbException("Unexpected error! No rows affected");
			}
		}catch(SQLException e){
			throw new DbException(e.getMessage());
			
		}finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public void update(Seller obj) {
		
	PreparedStatement st = null;
		
		try {
			st = conn.prepareStatement(
					"UPDATE seller "
					+ "SET Name = ?, Email = ?, BirthDate = ?, BaseSalary = ?, DepartmentId = ? "
					+ "WHERE Id = ?	");		
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			st.setInt(6, obj.getId());
			
			st.executeUpdate();
			
		}catch(SQLException e){
			throw new DbException(e.getMessage());
			
		}finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public void deleteById(Integer id) {
		
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("DELETE FROM seller\r\n" + 
					"WHERE Id = ?");
			st.setInt(1, id);
			st.executeUpdate();
			
		}catch(SQLException e){
			throw new DbException(e.getMessage());
		}finally {
			DB.closeStatement(st);
		}
	}

	@Override
	public Seller findById(Integer id) {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
				"SELECT seller.*,department.Name as DepName "  
				+ "FROM seller INNER JOIN department "  
				+ "ON seller.DepartmentId = department.Id "  
				+ "WHERE seller.Id = ?"	);
			st.setInt(1, id);
			rs = st.executeQuery();
			if (rs.next()) {
				Department dep = instantiateDepartment(rs);
				Seller obj = instanciateSeller(rs, dep);
				
				return obj;
			}
			return null;
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	
	}

	private Seller instanciateSeller(ResultSet rs, Department dep) throws SQLException {
		Seller obj = new Seller();
		obj.setId(rs.getInt("Id"));
		obj.setName(rs.getString("Name"));
		obj.setEmail(rs.getString("Email"));
		obj.setBaseSalary(rs.getDouble("BaseSalary"));
		obj.setBirthDate(new java.util.Date(rs.getTimestamp("BirthDate").getTime()));
		obj.setDepartment(dep);
		return obj;
	}
	private Department instantiateDepartment(ResultSet rs) throws SQLException {
		Department dep = new Department();
		dep.setId(rs.getInt("DepartmentId"));
		dep.setName(rs.getString("DepName"));
		return dep;
	}
	@Override
	public List<Seller> findAll() {
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
				"SELECT seller.*,department.Name as DepName "
				+ "FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id "
				+ "ORDER BY Name");
			
			rs = st.executeQuery();
			//List para salvar os vendedores do departamento
			List <Seller> list = new ArrayList<>();
			//O Map vai restringir que o departamento seja instanciado para cada elemento da lista
			Map<Integer, Department> map = new HashMap<>();
			
			//while vai funcionar enquanto tiver dados no rs
			while (rs.next()) {
				//Instancia o departamento no map, se não existir vai retornar nulo
				Department dep = map.get(rs.getInt("DepartmentId"));
				//se o map não retornar nulo, vai pular o if e nã vai instanciar o dep novamente
				if( dep == null) {
					dep =instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				Seller obj = instanciateSeller(rs, dep);
				list.add(obj);
				
			}
			return list;
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}	

	}
	@Override
	public List<Seller> findByDepartment(Department department) {
		
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = conn.prepareStatement(
				"SELECT seller.*,department.Name as DepName "
				+ "FROM seller INNER JOIN department "
				+ "ON seller.DepartmentId = department.Id "
				+ "WHERE DepartmentId = ? "
				+ "ORDER BY Name");
			st.setInt(1, department.getId());
			rs = st.executeQuery();
			//List para salvar os vendedores do departamento
			List <Seller> list = new ArrayList<>();
			//O Map vai restringir que o departamento seja instanciado para cada elemento da lista
			Map<Integer, Department> map = new HashMap<>();
			
			//while vai funcionar enquanto tiver dados no rs
			while (rs.next()) {
				//Instancia o departamento no map, se não existir vai retornar nulo
				Department dep = map.get(rs.getInt("DepartmentId"));
				//se o map não retornar nulo, vai pular o if e nã vai instanciar o dep novamente
				if( dep == null) {
					dep =instantiateDepartment(rs);
					map.put(rs.getInt("DepartmentId"), dep);
				}
				Seller obj = instanciateSeller(rs, dep);
				list.add(obj);
				
			}
			return list;
			
		}catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}	
	
	}

}
