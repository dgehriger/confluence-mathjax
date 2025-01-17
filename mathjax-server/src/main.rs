#![feature(proc_macro_hygiene, decl_macro)]

#[macro_use]
extern crate rocket;

use std::io::Cursor;

use mathjax::MathJax;
use rocket::http::{ContentType, Status};
use rocket::{Build, Rocket};
extern crate lazy_static;
use lazy_static::lazy_static;

// Initialize MathJax renderer once
lazy_static! {
    static ref RENDERER: MathJax = MathJax::new().unwrap();
}

#[derive(FromForm)]
struct MathJaxData {
    mathjax: String,
}

#[derive(FromForm)]
struct MathJaxPngData {
    mathjax: String,
    scaling: f32,
}

#[post("/svg", data = "<data>")]
fn render_math_svg(data: rocket::form::Form<MathJaxData>) -> (Status, (ContentType, String)) {
    let mathjax = &data.mathjax;
    println!("Received request: {:?}", mathjax);

    match RENDERER.render(mathjax) {
        Ok(result) => {
            let svg_string = result.into_raw();

            (Status::Ok, (ContentType::SVG, svg_string))
        }
        Err(err) => (
            Status::InternalServerError,
            (ContentType::Plain, err.to_string()),
        ),
    }
}

#[post("/png", data = "<data>")]
fn render_math_png(data: rocket::form::Form<MathJaxPngData>) -> Result<Vec<u8>, Status> {
    let mathjax = &data.mathjax;
    let scaling = data.scaling;

    match RENDERER.render(mathjax) {
        Ok(result) => {
            let image = match result.into_image(scaling) {
                Ok(image) => image,
                Err(_err) => return Err(Status::InternalServerError),
            };

            // store as PNG
            let mut buffer = Cursor::new(Vec::new());
            image
                .write_to(&mut buffer, image::ImageFormat::Png)
                .unwrap();
            Ok(buffer.into_inner())
        }

        Err(_err) => Err(Status::InternalServerError),
    }
}

#[launch]
fn rocket() -> Rocket<Build> {
    let config = rocket::Config {
        port: 43603,
        ..Default::default()
    };

    rocket::custom(config).mount("/", routes![render_math_svg, render_math_png])
}
