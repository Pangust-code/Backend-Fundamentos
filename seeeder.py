import psycopg2
from faker import Faker
import random
from datetime import datetime
import bcrypt

# ================= CONFIGURACIÓN =================
DB_HOST = "localhost"
DB_NAME = "devdb"
DB_USER = "ups"
DB_PASS = "ups123"
DB_PORT = "5432"

# Cantidad de datos
NUM_USERS = 50
NUM_CATEGORIES = 20
NUM_PRODUCTS = 1000  # Total de productos a generar

fake = Faker('es_ES')

# ================= CONEXIÓN =================
try:
    conn = psycopg2.connect(
        host=DB_HOST,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASS,
        port=DB_PORT
    )
    cur = conn.cursor()
    print("✅ Conectado a PostgreSQL")
except Exception as e:
    print(f"❌ Error de conexión: {e}")
    exit()

def get_now():
    return datetime.now()

# ================= 1. GENERAR USUARIOS =================
def seed_users():
    print(f"👤 Generando {NUM_USERS} usuarios...")
    ids = []
    
    # Generamos hash una sola vez para rendimiento (Pass: "123456")
    salt = bcrypt.gensalt()
    hashed_pw = bcrypt.hashpw("123456".encode('utf-8'), salt).decode('utf-8')

    sql = """
        INSERT INTO users (name, email, password, created_at, updated_at, deleted) 
        VALUES (%s, %s, %s, %s, %s, %s) RETURNING id;
    """

    for _ in range(NUM_USERS):
        try:
            cur.execute(sql, (
                fake.name(), 
                fake.unique.email(), 
                hashed_pw, 
                get_now(), 
                None,  # updated_at nulo al inicio
                False  # deleted false
            ))
            ids.append(cur.fetchone()[0])
        except Exception as e:
            conn.rollback()
            print(f"   ⚠️ Saltando usuario duplicado o error: {e}")

    conn.commit()
    return ids

# ================= 2. GENERAR CATEGORÍAS =================
def seed_categories():
    print(f"🏷️ Generando {NUM_CATEGORIES} categorías...")
    ids = []
    
    sql = """
        INSERT INTO categories (name, description, created_at, updated_at, deleted) 
        VALUES (%s, %s, %s, %s, %s) RETURNING id;
    """

    for _ in range(NUM_CATEGORIES):
        try:
            name = fake.unique.job()
            if len(name) > 120: name = name[:120]
            
            cur.execute(sql, (
                name, 
                fake.sentence(nb_words=10), 
                get_now(), 
                None, 
                False
            ))
            ids.append(cur.fetchone()[0])
        except Exception as e:
            conn.rollback() 
            # Es normal que falle si Faker repite un nombre 'unique'

    conn.commit()
    return ids

# ================= 3. GENERAR PRODUCTOS =================
def seed_products(user_ids, category_ids):
    print(f"📦 Generando {NUM_PRODUCTS} productos...")
    
    if not user_ids or not category_ids:
        print("❌ Faltan usuarios o categorías. Abortando.")
        return

    sql_product = """
        INSERT INTO products (name, price, description, user_id, created_at, updated_at, deleted) 
        VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING id;
    """
    
    sql_relation = """
        INSERT INTO product_categories (product_id, category_id) VALUES (%s, %s);
    """

    for i in range(NUM_PRODUCTS):
        try:
            # Datos aleatorios
            owner_id = random.choice(user_ids)
            price = round(random.uniform(10.0, 5000.0), 2)
            # Opción A: Nombre estilo frase corta (ej: "Mesa de madera")
            name = fake.sentence(nb_words=3).replace(".", "")
            
            # Insertar Producto
            cur.execute(sql_product, (
                name, 
                price, 
                fake.paragraph(nb_sentences=2), 
                owner_id, 
                get_now(), 
                None, 
                False
            ))
            product_id = cur.fetchone()[0]
            
            # Asignar 1 a 3 categorías aleatorias
            cats_for_prod = random.sample(category_ids, k=random.randint(1, 3))
            
            for cat_id in cats_for_prod:
                cur.execute(sql_relation, (product_id, cat_id))
            
            # Progreso visual simple cada 100
            if i % 100 == 0: print(f"   ... {i} productos insertados")
            
        except Exception as e:
            conn.rollback()
            print(f"Error en producto {i}: {e}")

    conn.commit()
    print("✅ Productos y relaciones creadas.")

# ================= EJECUCIÓN =================
if __name__ == "__main__":
    # Asegúrate que Spring Boot ya creó las tablas (ddl-auto: create/update)
    print("--- INICIANDO SEEDER ---")
    
    u_ids = seed_users()
    c_ids = seed_categories()
    
    if len(u_ids) > 0 and len(c_ids) > 0:
        seed_products(u_ids, c_ids)
    else:
        print("❌ No se pudieron crear usuarios o categorías base.")
        
    cur.close()
    conn.close()
    print("--- FINALIZADO ---")