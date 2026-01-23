import psycopg2
import sys

# Configuración de conexión
DB_CONFIG = {
    'host': 'localhost',
    'database': 'devdb',
    'user': 'ups',
    'password': 'ups123',
    'port': '5432'
}

def execute_sql_file(filename):
    """Ejecuta un archivo SQL y muestra los resultados"""
    try:
        # Conectar a PostgreSQL
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        
        # Leer el archivo SQL
        with open(filename, 'r', encoding='utf-8') as f:
            sql = f.read()
        
        print(f"\n{'='*80}")
        print(f"Ejecutando: {filename}")
        print(f"{'='*80}\n")
        
        # Ejecutar la consulta
        cur.execute(sql)
        
        # Obtener y mostrar resultados
        results = cur.fetchall()
        
        # Mostrar encabezados de columnas
        if cur.description:
            headers = [desc[0] for desc in cur.description]
            print("\t".join(headers))
            print("-" * 80)
        
        # Mostrar resultados
        for row in results:
            print("\t".join(str(cell) for cell in row))
        
        print(f"\n{'='*80}")
        print(f"✅ Consulta ejecutada exitosamente. Filas: {len(results)}")
        print(f"{'='*80}\n")
        
        cur.close()
        conn.close()
        
    except FileNotFoundError:
        print(f"❌ Error: Archivo '{filename}' no encontrado")
        sys.exit(1)
    except psycopg2.Error as e:
        print(f"❌ Error de base de datos: {e}")
        sys.exit(1)
    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python run_sql.py <archivo.sql>")
        print("Ejemplo: python run_sql.py 'EXPLAIN ANALYZE.sql'")
        sys.exit(1)
    
    sql_file = sys.argv[1]
    execute_sql_file(sql_file)
