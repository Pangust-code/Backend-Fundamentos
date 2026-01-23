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
    """Retorna el timestamp actual.
    Nota: Spring Boot usa @PrePersist para created_at automáticamente,
    pero este seeder los establece manualmente para control total."""
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

    for i in range(NUM_USERS):
        try:
            # Nombre secuencial: usuario-1, usuario-2, etc.
            name = f"usuario-{i + 1}"
            email = f"usuario{i + 1}@example.com"
            
            cur.execute(sql, (
                name, 
                email, 
                hashed_pw, 
                get_now(), 
                None,  # updated_at nulo al inicio
                False  # deleted false
            ))
            ids.append(cur.fetchone()[0])
        except Exception as e:
            conn.rollback()
            # Es normal que algunos usuarios fallen por email duplicado

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

    for i in range(NUM_CATEGORIES):
        try:
            # Nombre secuencial: categoria-1, categoria-2, etc.
            name = f"categoria-{i + 1}"
            description = f"Descripción de la categoría {i + 1}"
            
            cur.execute(sql, (
                name, 
                description, 
                get_now(), 
                None, 
                False
            ))
            ids.append(cur.fetchone()[0])
        except Exception as e:
            conn.rollback()
            # Error al insertar categoría

    conn.commit()
    return ids

# ================= 3. GENERAR PRODUCTOS =================
def seed_products(user_ids, category_ids):
    print(f"📦 Generando {NUM_PRODUCTS} productos...")
    
    if not user_ids or not category_ids:
        print("❌ Faltan usuarios o categorías. Abortando.")
        return

    # Listas de nombres realistas para generar productos variados
    product_types = [
        "Laptop", "Mouse", "Teclado", "Monitor", "Auriculares", "Webcam", 
        "Micrófono", "Tablet", "Smartphone", "Smartwatch", "Cargador",
        "Cable USB", "Hub USB", "Disco Duro", "SSD", "Memoria RAM",
        "Tarjeta Gráfica", "Procesador", "Placa Base", "Fuente de Poder",
        "Gabinete", "Cooler", "Pasta Térmica", "Adaptador", "Router",
        "Switch", "Access Point", "Impresora", "Scanner", "Proyector"
    ]
    
    brands = [
        "Dell", "HP", "Lenovo", "Asus", "Acer", "MSI", "Razer", "Logitech",
        "Corsair", "Kingston", "Samsung", "LG", "Sony", "Apple", "Xiaomi",
        "Huawei", "Motorola", "Nokia", "Canon", "Epson", "Brother",
        "TP-Link", "Netgear", "D-Link", "Cisco", "Intel", "AMD", "Nvidia"
    ]
    
    adjectives = [
        "Pro", "Gaming", "Premium", "Ultra", "Deluxe", "Plus", "Max",
        "Elite", "Advanced", "Professional", "Wireless", "RGB", "Mecánico",
        "Ergonómico", "Portátil", "Compacto", "HD", "4K", "Bluetooth"
    ]

    sql_product = """
        INSERT INTO products (name, price, description, user_id, created_at, updated_at, deleted) 
        VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING id;
    """
    
    sql_relation = """
        INSERT INTO product_categories (product_id, category_id) VALUES (%s, %s);
    """

    for i in range(NUM_PRODUCTS):
        try:
            # Datos aleatorios para owner y precio
            owner_id = random.choice(user_ids)
            price = round(random.uniform(10.0, 5000.0), 2)
            
            # Generar nombre realista y variado
            product_type = random.choice(product_types)
            brand = random.choice(brands)
            adjective = random.choice(adjectives) if random.random() > 0.3 else ""
            
            # Combinaciones realistas
            if adjective:
                name = f"{product_type} {brand} {adjective} {i+1}"
            else:
                name = f"{product_type} {brand} {i+1}"
            
            description = f"{product_type} de alta calidad marca {brand}. Ideal para uso profesional y gaming."
            
            # Insertar Producto
            cur.execute(sql_product, (
                name, 
                price, 
                description, 
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
            
            # Progreso visual cada 100 productos
            if (i + 1) % 100 == 0: print(f"   ... {i + 1}/{NUM_PRODUCTS} productos insertados")
            
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